Wikipedia River Plugin for Elasticsearch
==================================

The Wikipedia River plugin allows index wikipedia.

In order to install the plugin, simply run: `bin/plugin -install elasticsearch/elasticsearch-river-wikipedia/2.0.0.RC1`.
Don't forget to restart the elasticsearch service - otherwise you will receive errors when trying to create index.

|   Wikipedia River Plugin   |    elasticsearch    | Release date |
|----------------------------|---------------------|:------------:|
| 2.0.0-SNAPSHOT (master)    | 1.0.0.RC1 -> master |              |
| 2.0.0.RC1                  | 1.0.0.RC1 -> master |  2014-01-15  |
| 1.4.0-SNAPSHOT (1.x)       | 0.90.3 -> 0.90      |              |
| 1.3.0                      | 0.90.3 -> 0.90      |  2013-11-27  |
| 1.2.0                      | 0.90.3 -> 0.90      |  2013-08-19  |
| 1.1.0                      | 0.19 -> 0.90.2      |  2012-02-07  |
| 1.0.0                      | 0.18                |  2012-12-05  |

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
        "url" : "uri to bz2 file in the wikipedia dump, or on your local hd"
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
