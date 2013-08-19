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

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.Test;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 *
 */
public class WikipediaRiverTest {

    protected ESLogger logger = ESLoggerFactory.getLogger(WikipediaRiverTest.class.getName());

    @Test
    public void testWikipediaRiver() throws IOException, InterruptedException {
        // We can now create our node and our river
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("gateway.type", "none")
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 0)
                .build();
        Node node = NodeBuilder.nodeBuilder().local(true).settings(settings).node();

        // We first remove existing index if any
        try {
            node.client().admin().indices().prepareDelete("wikipedia").execute().actionGet();
        } catch (IndexMissingException e) {
            // Index is missing? It's perfectly fine!
        }

        // Let's create an index for our docs and we will disable refresh
        node.client().admin().indices().prepareCreate("wikipedia").execute().actionGet();

        XContentBuilder river = jsonBuilder()
                .startObject()
                    .field("type", "wikipedia")
                    .startObject("index")
                        .field("bulk_size", 1)
                    .endObject()
                .endObject();

        node.client().prepareIndex("_river", "wikipedia", "_meta").setSource(river).execute().actionGet();

        Thread.sleep(5000);

        // After some seconds, we should have some documents
        CountResponse countResponse = node.client().prepareCount("wikipedia").execute().actionGet();

        // It could happen that we don't have internet access when building wikipedia river
        // So we won't fail but only display a WARN message
        if (countResponse.getCount() == 0) {
            logger.warn("No page is indexed. If you don't have internet access, forget this message. " +
                    "Otherwise you should fix the issue!");
        }
    }


}
