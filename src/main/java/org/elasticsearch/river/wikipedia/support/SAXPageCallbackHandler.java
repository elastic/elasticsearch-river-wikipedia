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

package org.elasticsearch.river.wikipedia.support;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A Wrapper class for the PageCallbackHandler
 *
 * @author Jason Smith
 */
public class SAXPageCallbackHandler extends DefaultHandler {

    private PageCallbackHandler pageHandler;
    private WikiPage currentPage;
    private String currentTag;

    private String currentWikitext;
    private String currentTitle;
    private String currentID;

    public SAXPageCallbackHandler(PageCallbackHandler ph) {
        pageHandler = ph;
    }

    public void startElement(String uri, String name, String qName, Attributes attr) {
        currentTag = qName;
        if (qName.equals("page")) {
            currentPage = new WikiPage();
            currentWikitext = "";
            currentTitle = "";
            currentID = "";
        }
    }

    public void endElement(String uri, String name, String qName) {
        if (qName.equals("page")) {
            currentPage.setTitle(currentTitle);
            currentPage.setID(currentID);
            currentPage.setWikiText(currentWikitext);
            pageHandler.process(currentPage);
        }
        if (qName.equals("mediawiki")) {
            // TODO hasMoreElements() should now return false
        }
    }

    public void characters(char ch[], int start, int length) {
        if (currentTag.equals("title")) {
            currentTitle = currentTitle.concat(new String(ch, start, length));
        }
        // TODO: To avoid looking at the revision ID, only the first ID is taken.
        // I'm not sure how big the block size is in each call to characters(),
        // so this may be unsafe.
        else if ((currentTag.equals("id")) && (currentID.length() == 0)) {
            currentID = new String(ch, start, length);
        } else if (currentTag.equals("text")) {
            currentWikitext = currentWikitext.concat(new String(ch, start, length));
        }
    }
}
