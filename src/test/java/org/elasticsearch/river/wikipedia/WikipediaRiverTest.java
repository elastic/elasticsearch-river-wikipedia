/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import org.elasticsearch.common.base.Predicate;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.plugins.PluginsService;
import org.elasticsearch.river.wikipedia.helper.HttpClient;
import org.elasticsearch.river.wikipedia.helper.HttpClientResponse;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.elasticsearch.test.junit.annotations.Network;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_REPLICAS;
import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_SHARDS;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * This test requires internet connexion
 * If you want to run this test, use -Dtests.network=true
 */
@ElasticsearchIntegrationTest.ClusterScope(
        scope = ElasticsearchIntegrationTest.Scope.SUITE, transportClientRatio = 0.0)
@Network
public class WikipediaRiverTest extends ElasticsearchIntegrationTest {

    @Override
    protected Settings nodeSettings(int nodeOrdinal) {
        return ImmutableSettings.builder()
                .put(super.nodeSettings(nodeOrdinal))
                .put("plugins." + PluginsService.LOAD_PLUGIN_FROM_CLASSPATH, true)
                .build();
    }

    @Before
    public void createEmptyRiverIndex() {
        // We want to force _river index to use 1 shard 1 replica
        client().admin().indices().prepareCreate("_river").setSettings(ImmutableSettings.builder()
                .put(SETTING_NUMBER_OF_SHARDS, 1)
                .put(SETTING_NUMBER_OF_REPLICAS, 0)).get();
    }

    @After
    public void deleteRiverAndWait() throws InterruptedException {
        logger.info(" --> remove all wikipedia rivers");
        client().admin().indices().prepareDelete("_river").get();
        // We just wait a few to make sure that all bulks has been processed
        awaitBusy(new Predicate<Object>() {
            @Override
            public boolean apply(Object o) {
                return false;
            }
        }, 2, TimeUnit.SECONDS);
    }

    private boolean isUrlAccessible(String server, String url) {
        HttpClientResponse response = new HttpClient(server, 80).request("HEAD", url);
        if (response.errorCode() == 200) {
            logger.info("  -> Internet working for [{}{}]", server, url);
            return true;
        } else {
            logger.info("  -> Internet not working for [{}{}]: {}", server, url, response.errorCode());
            return false;
        }
    }

    @Test
    public void testWikipediaRiver() throws IOException, InterruptedException {
        if (isUrlAccessible("download.wikimedia.org", "/enwiki/latest/enwiki-latest-pages-articles.xml.bz2")) {
            logger.info(" --> create wikipedia river");
            index("_river", "wikipedia", "_meta", jsonBuilder()
                    .startObject()
                    .field("type", "wikipedia")
                    .startObject("index")
                    .field("bulk_size", 100)
                    .field("flush_interval", "100ms")
                    .endObject()
                    .endObject());

            logger.info(" --> waiting for some documents");
            // Check that docs are indexed by the river
            assertThat(awaitBusy(new Predicate<Object>() {
                public boolean apply(Object obj) {
                    try {
                        refresh();
                        CountResponse response = client().prepareCount("wikipedia").get();
                        logger.info("  -> got {} docs in {} index", response.getCount());
                        return response.getCount() > 0;
                    } catch (IndexMissingException e) {
                        return false;
                    }
                }
            }, 1, TimeUnit.MINUTES), equalTo(true));
        }
    }

    /**
     * Testing another wikipedia source
     * http://dumps.wikimedia.org/frwiki/latest/frwiki-latest-pages-articles.xml.bz2
     */
    @Test
    public void testWikipediaRiverFrench() throws IOException, InterruptedException {
        if (isUrlAccessible("dumps.wikimedia.org", "/frwiki/latest/frwiki-latest-pages-articles.xml.bz2")) {
            logger.info(" --> create wikipedia river");
            index("_river", "wikipedia", "_meta", jsonBuilder()
                    .startObject()
                        .field("type", "wikipedia")
                    .startObject("wikipedia")
                    .field("url", "http://dumps.wikimedia.org/frwiki/latest/frwiki-latest-pages-articles.xml.bz2")
                    .endObject()
                    .startObject("index")
                    .field("bulk_size", 100)
                    .field("flush_interval", "1s")
                        .endObject()
                    .endObject());

            logger.info(" --> waiting for some documents");
            // Check that docs are indexed by the river
            assertThat(awaitBusy(new Predicate<Object>() {
                public boolean apply(Object obj) {
                    try {
                        refresh();
                        CountResponse response = client().prepareCount("wikipedia").get();
                        logger.info("  -> got {} docs in {} index", response.getCount());
                        return response.getCount() > 0;
                    } catch (IndexMissingException e) {
                        return false;
                    }
                }
            }, 1, TimeUnit.MINUTES), equalTo(true));
        }
    }
}
