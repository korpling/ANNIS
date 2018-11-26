# Document Browser

The default configuration for the document browser is stored in the
`conf/document_browser.json` file in the *back-end configuration*. 
It can be overwritten by a custom the `document_browser.json` file 
placed in the `ExtData` directory of a corpus.

## Automatic switch on/off

The ANNIS importer tries to detect an artificial token
segmentation. If the `text.annis` import file contains 
only artificial token (which means there are only white spaces) the 
document browser is disabled. 
In the case there exists a `document_browser.json` file
which configures the document browser it will never be disabled by
ANNIS. Also if in the `corpus.properties` the `browse-documents`
properties is set to true, the document browser will stay active.

## Custom visualizer and sorting

It is also possible to use a custom visualizer for browsing a whole
document. The configuration is in JSON-Syntax file named
document_browser.json, which can be add to the ExtData directory of
each corpus.

~~~json
{
    "visualizers": [
        {
            "type"  : "raw_text",
            "displayName" :  "full text",
            "mappings" : "vertical:true"
        }
    ],

    "metaDataColumns" : [
	{
	    "namespace" : "annis",
	    "name" : "Genre"
	}
    ],

    "orderBy" : [
	{
	    "namespace" : "annis",
	    "name" :"Titel",
	    "ascending" : "false"}
    ]
}
~~~

Explanation in detail:

* visualizers: Defines which document visualizers are available for a
   corpus. All visualizer from the list above with the suffix "doc" in
   their name are suitable for using as doc visualizer.

* metaDataColumns (optional): For every defined metadata object an
  additional column is generated with the metadata key as column
  header and the metadata value as table cell value. The subfield
  'namespace' is optional.

* orderBy (optional): In the default state the table is sorted by
  document name. But it's also possible to define a custom sort by the
  metadata fields, even if the column is not visible. 'namespace' and
  'ascending' is optional. 'ascending' its default setting is 'true'.

