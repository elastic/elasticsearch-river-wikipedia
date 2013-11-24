/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.river.wikipedia;

import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.river.AbstractRiverComponent;
import org.elasticsearch.river.River;
import org.elasticsearch.river.RiverName;
import org.elasticsearch.river.RiverSettings;
import org.elasticsearch.river.wikipedia.support.PageCallbackHandler;
import org.elasticsearch.river.wikipedia.support.WikiPage;
import org.elasticsearch.river.wikipedia.support.WikiXMLParser;
import org.elasticsearch.river.wikipedia.support.WikiXMLParserFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 *
 */
public class WikipediaRiver extends AbstractRiverComponent implements River {

    private StringBuilder sb = new StringBuilder();

    private final Client client;

    private final URL url;

    private final String indexName;

    private final String typeName;

    private final int bulkSize;

    private volatile Thread thread;

    private volatile boolean closed = false;

    private final TimeValue bulkFlushInterval;
    private volatile BulkProcessor bulkProcessor;
    private final int maxConcurrentBulk;


    @SuppressWarnings({"unchecked"})
    @Inject
    public WikipediaRiver(RiverName riverName, RiverSettings settings, Client client) throws MalformedURLException {
        super(riverName, settings);
        this.client = client;

        String url = "http://download.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2";
        if (settings.settings().containsKey("wikipedia")) {
            Map<String, Object> wikipediaSettings = (Map<String, Object>) settings.settings().get("wikipedia");
            url = XContentMapValues.nodeStringValue(wikipediaSettings.get("url"), url);
        }

        logger.info("creating wikipedia stream river for [{}]", url);
        this.url = new URL(url);

        if (settings.settings().containsKey("index")) {
            Map<String, Object> indexSettings = (Map<String, Object>) settings.settings().get("index");
            this.indexName = XContentMapValues.nodeStringValue(indexSettings.get("index"), riverName.name());
            this.typeName = XContentMapValues.nodeStringValue(indexSettings.get("type"), "page");
            this.bulkSize = XContentMapValues.nodeIntegerValue(indexSettings.get("bulk_size"), 100);
            this.bulkFlushInterval = TimeValue.parseTimeValue(XContentMapValues.nodeStringValue(
                    indexSettings.get("flush_interval"), "5s"), TimeValue.timeValueSeconds(5));
            this.maxConcurrentBulk = XContentMapValues.nodeIntegerValue(indexSettings.get("max_concurrent_bulk"), 1);
        } else {
            this.indexName = riverName.name();
            this.typeName = "page";
            this.bulkSize = 100;
            this.maxConcurrentBulk = 1;
            this.bulkFlushInterval = TimeValue.timeValueSeconds(5);
        }
    }

    @Override
    public void start() {
        logger.info("starting wikipedia stream");
        try {
            client.admin().indices().prepareCreate(indexName).execute().actionGet();
        } catch (Exception e) {
            if (ExceptionsHelper.unwrapCause(e) instanceof IndexAlreadyExistsException) {
                // that's fine
            } else if (ExceptionsHelper.unwrapCause(e) instanceof ClusterBlockException) {
                // ok, not recovered yet..., lets start indexing and hope we recover by the first bulk
                // TODO: a smarter logic can be to register for cluster event listener here, and only start sampling when the block is removed...
            } else {
                logger.warn("failed to create index [{}], disabling river...", e, indexName);
                return;
            }
        }
        WikiXMLParser parser = WikiXMLParserFactory.getSAXParser(url);
        try {
            parser.setPageCallback(new PageCallback());
        } catch (Exception e) {
            logger.error("failed to create parser", e);
            return;
        }

        // Creating bulk processor
        this.bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                logger.debug("Going to execute new bulk composed of {} actions", request.numberOfActions());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                logger.debug("Executed bulk composed of {} actions", request.numberOfActions());
                if (response.hasFailures()) {
                    logger.warn("There was failures while executing bulk", response.buildFailureMessage());
                    if (logger.isDebugEnabled()) {
                        for (BulkItemResponse item : response.getItems()) {
                            if (item.isFailed()) {
                                logger.debug("Error for {}/{}/{} for {} operation: {}", item.getIndex(),
                                        item.getType(), item.getId(), item.getOpType(), item.getFailureMessage());
                            }
                        }
                    }
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                logger.warn("Error executing bulk", failure);
            }
        })
                .setBulkActions(bulkSize)
                .setConcurrentRequests(maxConcurrentBulk)
                .setFlushInterval(bulkFlushInterval)
                .build();

        thread = EsExecutors.daemonThreadFactory(settings.globalSettings(), "wikipedia_slurper").newThread(new Parser(parser));
        thread.start();
    }

    @Override
    public void close() {
        logger.info("closing wikipedia river");
        closed = true;
        if (thread != null) {
            thread.interrupt();
        }

        if (this.bulkProcessor != null) {
            this.bulkProcessor.close();
        }
    }

    private class Parser implements Runnable {
        private final WikiXMLParser parser;

        private Parser(WikiXMLParser parser) {
            this.parser = parser;
        }

        @Override
        public void run() {
            try {
                parser.parse();
            } catch (Exception e) {
                if (closed) {
                    return;
                }
                logger.error("failed to parse stream", e);
            }
        }
    }

    private class PageCallback implements PageCallbackHandler {

        @Override
        public void process(WikiPage page) {
            if (closed) {
                return;
            }
            String title = stripTitle(page.getTitle());
            if (logger.isTraceEnabled()) {
                logger.trace("page {} : {}", page.getID(), page.getTitle());
            }
            try {
                XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
                builder.field("title", title);
                builder.field("text", page.getText());
                builder.field("redirect", page.isRedirect());
                builder.field("redirectPage", page.getRedirectPage());
                builder.field("special", page.isSpecialPage());
                builder.field("stub", page.isStub());
                builder.field("disambiguation", page.isDisambiguationPage());

                builder.startArray("category");
                for (String s : page.getCategories()) {
                    builder.value(s);
                }
                builder.endArray();

                builder.startArray("link");
                for (String s : page.getLinks()) {
                    builder.value(s);
                }
                builder.endArray();

                builder.endObject();

                bulkProcessor.add(new IndexRequest(indexName, typeName, page.getID()).source(builder));
            } catch (Exception e) {
                logger.warn("failed to construct index request", e);
            }
        }
    }


    private String stripTitle(String title) {
        sb.setLength(0);
        sb.append(title);
        while (sb.length() > 0 && (sb.charAt(sb.length() - 1) == '\n' || (sb.charAt(sb.length() - 1) == ' '))) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
