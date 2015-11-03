ANNIS import format version 3.3 {#dev-annisimportformat}
===================================

[TOC]

Remarkable differences to Salt
==============================

The ANNIS import format is inspired by the Salt meta-data model an ANNIS uses Salt internally to represent a matched graph from the database. However there are some restrictions which ANNIS has but Salt doesn't.

* node names must be unique per document
* document names must be unique per top-level corpus
* a ANNIS corpus contains only one top-level corpus
* there are no meta-data for nodes
* string identifiers such as annotation or layer names have a limited number of allowed characters and should match the regular expression
  \verbatim
[a-zA-Z_][a-zA-Z0-9_-]*
  \endverbatim
  in order to be searchable with AQL

File format
============

A ANNIS corpus can be either a zip file or a directory.

Files inside zip file/directory
===============================

annis.version
----------------

First line is exactly "3.3", the next lines can contain human readable text.

_Pure UTF-8 encoded text file_

corpus.annis
-------------------

Contains structural information about the corpus and its documents.

_TAB-separated file as described in http://www.postgresql.org/docs/9.1/static/sql-copy.html#AEN64040_
|column | type | unique | not NULL | description |
|-------|------|--------|----------|------------|
|id|integer|X|X|primary key|
|name | text|X|X|unique name (per corpus)|
|type | text||X|CORPUS, DOCUMENT|
|version | text|||version number (currently not used)|
|pre|integer||X|pre order of the corpus tree|
|post|integer||X|post order of the corpus tree|
|top_level|boolean||X|true for the toplevel corpus|

corpus_annotation.annis
--------------------------

Contains meta-data on the corpus and the documents.

_TAB-separated file as described in http://www.postgresql.org/docs/9.1/static/sql-copy.html#AEN64040_
|column | type | unique | not NULL | description |
|-------|------|--------|----------|------------|
|corpus_ref|integer||X|foreign key to corpus.id|
|namespace | text| | | |
|name|text| | | |
|value|text| | | |

text.annis
-----------------

Describes all texts that are included in the corpus.

_TAB-separated file as described in http://www.postgresql.org/docs/9.1/static/sql-copy.html#AEN64040_
|column | type | unique | not NULL | description |
|-------|------|--------|----------|------------|
|corpus_ref|integer||X|foreign key to corpus.id. The corpus id should be the id of the document in the corpus table|
|id|integer| |X| restart from 0 for every corpus_ref|
|name|text| | |name of the text|
|text|text| | |content of the text|

primary key: corpus_ref, id

node.annis
-------------

Every node in the corpus will have exactly one entry in this table.

_TAB-separated file as described in http://www.postgresql.org/docs/9.1/static/sql-copy.html#AEN64040_
|column | type | unique | not NULL | description |
|-------|------|--------|----------|------------|
|id|bigint|X|X|primary key|
|text_ref|integer||X|foreign key to text.id|
|corpus_ref|integer||X|foreign key to corpus.id|
|layer|text| | | |
|name|text| | | A human readable identfier of the node. Must be unique for each document. |
|left|integer||X|position of first covered character|
|right|integer||X|position of the character after the last covered character|
|token_index|integer|||index of this token (if it is a token, otherwise NULL)|
|left_token|integer||X|index of first covered token, for token, this value is the token_index|
|right_token|integer||X|index of last covered token, for token, this value is the token_index|
|seg_index|integer|||index of this segment (if it is a segment, i.e. there is some SOrderingRelation connected to this node)|
|seg_name|text|||name of the segment path this segment belongs to|
|span|text|||for tokens or node with a segmentation index: substring of the covered original text|
|root|boolean||X|True if this node has no parents in *all* components|

component.annis
------------------

Lists the components (connected sub-graphs) of the graph.

_TAB-separated file as described in http://www.postgresql.org/docs/9.1/static/sql-copy.html#AEN64040_
|column | type | unique | not NULL | description |
|-------|------|--------|----------|------------|
|id|bigint|X|X|primary key|
|type|char(1)|||edge type: c, d, p|
|layer|text||X| Could be set to e.g. "default_layer" if not in any Salt layer|
|name|text|||The sType of the component, e.g anaphoric for a some kind of pointing relation component|

rank.annis
-------------

A rank entry describes one of the positions of a node in a component tree. There is one rank entry for each edge. Furthermore,
every component has a virtual relation and thus an additional rank entry where the parent attribute is NULL and the level is 0.

_TAB-separated file as described in http://www.postgresql.org/docs/9.1/static/sql-copy.html#AEN64040_
|column | type | unique | not NULL | description |
|-------|------|--------|----------|------------|
|id|bigint|X|X|primary key|
|pre|integer||X| the preorder of the target node. the root of the component tree should always have a pre-order of 0|
|post|integer||X|the post-order or the target node|
|node_ref|bigint||X|the node.id of the target node|
|component_ref|bigint||X||
|parent|bigint|||*id* of the parent rank entry|
|level|integer||| level of this rank entry (not node!) in the component tree | 

Rank entries with the type 'c' (coverage spans) *must* be ommitted, if the referenced node is a token and if the parent coverage span is continuous. Continuous means the range of covered token has no gaps, thus it includes all token between the first and the last covered token. The idea behind this is, that you can recover the needed information using the "left_token" and "right_token" from the span together with the "token_index" (all in the node.annis table) if the span is continuous.

node_annotation.annis
------------------------

Contains all annotations per node.

_TAB-separated file as described in http://www.postgresql.org/docs/9.1/static/sql-copy.html#AEN64040_
|column | type | unique | not NULL | description |
|-------|------|--------|----------|------------|
|node_ref|bigint||X|foreign key to _node.id|
|namespace|text| | | |
|name|text| |X| |
|value|text| | | |


unique(node_ref, namespace, name)

edge_annotation.annis
------------------------

Contains all annotations per edge (which is represented by a rank entry)

_TAB-separated file as described in http://www.postgresql.org/docs/9.1/static/sql-copy.html#AEN64040_
|column | type | unique | not NULL | description |
|-------|------|--------|----------|------------|
|rank_ref|bigint||X|foreign key to rank.id|
|namespace|text| | | |
|name|text||X| |
|value|text| | | |

resolver_vis_map.annis
-------------------------

Describes which visualizers to trigger depending of the namespace of a node or edge occuring in the search results.

_TAB-separated file as described in http://www.postgresql.org/docs/9.1/static/sql-copy.html#AEN64040_
|column | type | unique | not NULL | description |
|-------|------|--------|----------|------------|
|corpus|text||| the name of the supercorpus |
|version|text||| the version of the corpus |
|namespace|text||| the several layers of the  corpus |
|element|text||| the type of the entry: "node" or "edge" |
|vis_type|text||X| the abstract type of visualization: "tree", "discourse", "grid", ... |
|display_name|text||X| the name of the layer which shall be shown for display|
|visibility|text||| either "permanent", "visible", "hidden", "removed" or "preloaded", default is "hidden" |
|order|bigint||| the order of the layers, in which they shall be shown |
|mappings|text||| |

ExtData folder
---------------

Contains the media files that are connected with this corpus including their binary content.

Each file directly inside the ExtData folder belongs to the toplevel corpus.
A sub-folder corresponds to a document and each file inside a sub-folder belongs to the document with the same name.

