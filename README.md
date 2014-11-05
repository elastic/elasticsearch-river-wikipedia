Wikipedia River Plugin for Elasticsearch
==================================

The Wikipedia River plugin allows index wikipedia.

In order to install the plugin, run: 

```sh
bin/plugin -install elasticsearch/elasticsearch-river-wikipedia/2.4.1
```

You need to install a version matching your Elasticsearch version:

|       Elasticsearch    | Wikipedia River Plugin|                                                             Docs                                                                   |
|------------------------|-------------------|------------------------------------------------------------------------------------------------------------------------------------|
|    master              | Build from source | See below                                                                                                                          |
|    es-1.x              | Build from source | [2.5.0-SNAPSHOT](https://github.com/elasticsearch/elasticsearch-river-wikipedia/tree/es-1.x/#version-250-snapshot-for-elasticsearch-1x)|
|    es-1.4              |     2.4.1         | [2.4.1](https://github.com/elasticsearch/elasticsearch-river-wikipedia/tree/v2.4.1/#version-241-for-elasticsearch-14)                  |
|    es-1.3              |     2.3.0         | [2.3.0](https://github.com/elasticsearch/elasticsearch-river-wikipedia/tree/v2.3.0/#version-230-for-elasticsearch-13)                  |
|    es-1.2              |     2.2.0         | [2.2.0](https://github.com/elasticsearch/elasticsearch-river-wikipedia/tree/v2.2.0/#wikipedia-river-plugin-for-elasticsearch)      |
|    es-1.0              |     2.0.0         | [2.0.0](https://github.com/elasticsearch/elasticsearch-river-wikipedia/tree/v2.0.0/#wikipedia-river-plugin-for-elasticsearch)      |
|    es-0.90             |     1.3.0         | [1.3.0](https://github.com/elasticsearch/elasticsearch-river-wikipedia/tree/v1.3.0/#wikipedia-river-plugin-for-elasticsearch)      |

To build a `SNAPSHOT` version, you need to build it with Maven:

```bash
mvn clean install
plugin --install river-wikipedia \ 
       --url file:target/releases/elasticsearch-river-wikipedia-X.X.X-SNAPSHOT.zip
```

Create river
------------

A simple river to index [Wikipedia](http://en.wikipedia.org) (English pages). Create it using:

```sh
curl -XPUT localhost:9200/_river/my_river/_meta -d '
{
    "type" : "wikipedia"
}
'
```

The default download is the latest [wikipedia dump](http://download.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2). It can be changed using:

```javascript
{
    "type" : "wikipedia",
    "wikipedia" : {
        "url" : "url to link to wikipedia dump"
    }
}
```

The index name defaults to the river name, and the type defaults to `page`. Both can be changed in the index section:

```javascript
{
    "type" : "wikipedia",
    "index" : {
        "index" : "my_index",
        "type" : "my_type"
    }
}
```

Since 1.3.0, by default, `bulk` size is `100`. A bulk is flushed every `5s`. Number of concurrent requests allowed to be executed is 1.
You can modify those settings within index section:

```javascript
{
    "type" : "wikipedia",
    "index" : {
        "index" : "my_index",
        "type" : "my_type",
        "bulk_size" : 1000,
        "flush_interval" : "1s",
        "max_concurrent_bulk" : 3
    }
}
```

Mapping
-------

By default, wikipedia river will generate the following mapping:

```javascript
{
   "page": {
      "properties": {
         "category": {
            "type": "string"
         },
         "disambiguation": {
            "type": "boolean"
         },
         "link": {
            "type": "string"
         },
         "redirect": {
            "type": "boolean"
         },
         "redirect_page": {
            "type": "string"
         },
         "special": {
            "type": "boolean"
         },
         "stub": {
            "type": "boolean"
         },
         "text": {
            "type": "string"
         },
         "title": {
            "type": "string"
         }
      }
   }
}
```


License
-------

    This software is licensed under the Apache 2 license, quoted below.

    Copyright 2009-2014 Elasticsearch <http://www.elasticsearch.org>

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy of
    the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations under
    the License.
