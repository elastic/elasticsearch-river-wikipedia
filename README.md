Wikipedia River Plugin for Elasticsearch
==================================

The Wikipedia River plugin allows index wikipedia.

## Version 2.5.0-SNAPSHOT for Elasticsearch: 1.5

If you are looking for another version documentation, please refer to the 
[compatibility matrix](https://github.com/elasticsearch/elasticsearch-river-wikipedia/#wikipedia-river-plugin-for-elasticsearch).


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
