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

import java.util.Vector;

/**
 * A class to iterate the pages after the wikipedia XML file has been parsed with {@link WikiXMLDOMParser}.
 *
 * @author Delip Rao
 * @see WikiXMLDOMParser
 */
public class WikiPageIterator {

    private int currentPage = 0;
    private int lastPage = 0;
    Vector<WikiPage> pageList = null;

    public WikiPageIterator(Vector<WikiPage> list) {
        pageList = list;
        if (pageList != null)
            lastPage = pageList.size();
    }

    /**
     * @return true if there are more pages to be read
     */
    public boolean hasMorePages() {
        return (currentPage < lastPage);
    }

    /**
     * Reset the iterator.
     */
    public void reset() {
        currentPage = 0;
    }

    /**
     * Advances the iterator by one position.
     *
     * @return a {@link WikiPage}
     */
    public WikiPage nextPage() {
        if (hasMorePages())
            return pageList.elementAt(currentPage++);
        return null;
    }
}
