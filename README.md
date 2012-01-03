Wikipedia River Plugin for ElasticSearch
==================================

The Wikipedia River plugin allows index wikipedia.

In order to install the plugin, simply run: `bin/plugin -install elasticsearch/elasticsearch-river-wikipedia/1.0.0`.

    ---------------------------------------
    | Wikipedia Plugin | ElasticSearch    |
    ---------------------------------------
    | master           | 0.18 -> master   |
    ---------------------------------------
    | 1.0.0            | 0.18 -> master   |
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
	        "name" : "my_index",
	        "type" : "my_type",
	        "bulk_size" : 100
	    }
	}
