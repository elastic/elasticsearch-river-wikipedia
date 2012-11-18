Wikipedia River Plugin for ElasticSearch
==================================

The Wikipedia River plugin allows index wikipedia.

In order to install the plugin, simply run: `bin/plugin -install elasticsearch/elasticsearch-river-wikipedia/1.1.0`.

    ---------------------------------------
    | Wikipedia Plugin | ElasticSearch    |
    ---------------------------------------
    | master           | 0.19 -> master   |
    ---------------------------------------
    | 1.1.0            | 0.19 -> master   |
    ---------------------------------------
    | 1.0.0            | 0.18             |
    ---------------------------------------

A simple river to index [Wikipedia](http://en.wikipedia.org) (English pages). Create it using:

	curl -XPUT localhost:9200/_river/my_river/_meta -d '
	{
	    "type" : "wikipedia"
	}
	'

The default download is the latest [wikipedia dump](http://download.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2). It can be changed using:

	{
	    "type" : "wikipedia",
	    "wikipedia" : {
	        "url" : "url to link to wikipedia dump"
	    }
	}

The index name defaults to the river name, and the type defaults to page. Both can be changed in the index section:

	{
	    "type" : "wikipedia",
	    "index" : {
	        "index" : "my_index",
	        "type" : "my_type",
	        "bulk_size" : 100
	    }
	}

License
-------

    This software is licensed under the Apache 2 license, quoted below.

    Copyright 2009-2012 Shay Banon and ElasticSearch <http://www.elasticsearch.org>

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy of
    the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations under
    the License.
