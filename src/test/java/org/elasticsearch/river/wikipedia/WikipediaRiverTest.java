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
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.elasticsearch.test.junit.annotations.Network;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * This test requires internet connexion
 * If you want to run this test, use -Dtests.network=true
 */
@Network
public class WikipediaRiverTest extends ElasticsearchIntegrationTest {

    protected ESLogger logger = ESLoggerFactory.getLogger(WikipediaRiverTest.class.getName());

    @Override
    protected Settings nodeSettings(int nodeOrdinal) {
        return ImmutableSettings.settingsBuilder()
                .put("gateway.type", "none")
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 0)
                .build();
    }

    @Test
    public void testWikipediaRiver() throws IOException, InterruptedException {
        logger.info(" --> wipe indices");
        // We first remove existing index if any
        wipeIndices("wikipedia");

        logger.info(" --> create wikipedia index");
        createIndex("wikipedia");

        XContentBuilder river = jsonBuilder()
                .startObject()
                    .field("type", "wikipedia")
                    .startObject("index")
                        .field("bulk_size", 100)
                        .field("flush_interval", "1s")
                    .endObject()
                .endObject();

        logger.info(" --> create wikipedia river");
        index("_river", "wikipedia", "_meta", river);

        logger.info(" --> wait for 5s");
        // We wait 5s for some documents to be indexed
        Thread.sleep(5000);

        // After some seconds, we should have some documents
        CountResponse countResponse = client().prepareCount("wikipedia").execute().actionGet();
        logger.info(" --> we have indexed {} docs", countResponse.getCount());

        // It could happen that we don't have internet access when building wikipedia river
        // So we won't fail but only display a WARN message
        assertThat("some pages should be indexed.", countResponse.getCount(), Matchers.greaterThan(0L));
    }
}
