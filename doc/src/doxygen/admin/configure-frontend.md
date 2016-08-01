Configure front-end web-application {#admin-configure-webapp}
==========================

[TOC]

Configuring Tomcat or Jetty as application container {#admin-configure-container}
====================================================

We are providing a WAR-file that is deployable by every common Java Servlet
Container like Tomcat or Jetty. Please use the documentation of the web
application container of your choice how to deploy these war-files.

## Tomcat: UTF8 encoding in server.xml ## {#admin-configure-tomcat-utf8}

If using Tomcat make sure the UTF-8 encoding is used for URLs. Some
installations of Tomcat don't use UTF-8 for the encoding of the URLs and that will
cause problems when searching for non-ASCII characters. In order to avoid this
the Connector-configuration needs the property "URIEncoding" set to "UTF-8"
like in this example (`$CATALINA_HOME/server.xml`):

\code{.xml}
<Connector port="8080" protocol="HTTP/1.1"
connectionTimeout="20000"
URIEncoding="UTF-8"
redirectPort="8443"
executor="tomcatThreadPool" />
\endcode

General configuration advices {#admin-configure-general}
=============================

The ANNIS frontend will search in different folders for it's configuration.

Folder | Description
------ | -----------
`<Installation>/WEB-INF/conf/` | Default configuration inside the deployed web application folder. Should not be changed.
`$ANNIS_CFG` or `/etc/annis/` | The global configuration folder defined by the environment variable `ANNIS_CFG` or a default path if not set.
`~/.annis/` | User specific configuration inside the `.annis` sub-folder inside the home folder of the user who is running the frontend.

Configuration files can be either in the [Java Properties](http://en.wikipedia.org/w/index.php?title=.properties&oldid=521500688)
or [JSON](http://www.json.org/) format. Configuration files from the user directory can
overwrite the global configuration and the global configuration overwrites the
default configuration.

Create and configure instances {#admin-configure-instance}
==============================

When multiple corpora from different sources are hosted on one server it is often
still desired to group the corpora by their origin and present them differently.
You should not be forced to have an ANNIS frontend and service installation for
each of this groups. Instead the administrator can define so called instances.

An instance is defined by a JSON file inside the instances sub-folder in one of
the configuration locations. The name of the file also defines the instance name.
Thus the file `instances/falko.json` defines the instance named "falko".

\code{.json}
{
	"display-name": "Falko",
	"default-querybuilder": "tigersearch",
	"default-corpusset": "falko-essays",
	"corpus-sets": [
	{
		"name": "falko-essays",
		"corpus": [
		"falko-essay-l1",
		"falko-essay-l2"
		]
	},
	{
		"name": "falko-summaries",
		"corpus": [
		"falko-summary-l1",
		"falko-summary-l2"
		]
	}
	],
	"keyboard-layout" : "de",
	"login-on-start": "true"
}
\endcode

Each instance configuration can have a verbose display-name which is
displayed in the title of the browser window. `default-querybuilder` defines the
short name of the query builder you want to use. Currently only "tigersearch" and "flatquerybuilder" are
available, see [here](@ref dev-querybuilder) if you want to add your own query builder.
The `keyboard-layout` variable is used as the default value for the virtual keyboard of the AQL query box.
If `login-on-start` is set to `true` a login window is shown at each startup of the ANNIS search UI if the user is not logged in yet.

While any user can group corpora into corpus sets for their own, you can define
corpus sets for the whole instance. Each corpus set is an JSON-object with a
name and a list of corpora that belong to the corpus set.

Any defined instance is assigned a special URL at which it can be accessed:
`http://<server>/annis-gui/<instance-name>`. The default instance is
additionally accessible by not specifying any instance name in the URL. You can
configure your web server (e.g. Apache) to rewrite the URLs if you need a more
project specific and less "technical" URL (e.g. `http://<server>/falko`).

Configuring Visualizations {#admin-configure-vis}
==========================

By default, ANNIS displays all search results in the Key Word in Context (KWIC)
view in the search result window. Further visualizations, such as syntax trees or
grid views, are displayed by default based on the following namespaces:

Namespace| visualizer
-- | ---
tiger | [tree visualizer](@ref annis.visualizers.component.tree.TigerTreeVisualizer)
exmaralda | [grid visualizer](@ref annis.visualizers.component.grid.GridVisualizer)
mmax | [discourse view](@ref annis.visualizers.iframe.CorefVisualizer)
video | [video player](@ref annis.visualizers.component.VideoVisualizer)
audio | [audio player](@ref annis.visualizers.component.AudioVisualizer)

In these cases the namespaces are usually taken from the source format in which
the corpus was generated, and carried over into relAnnis during the conversion.
It is also possible to use other namespaces, most easily when working with
PAULA XML. In PAULA XML, the namespace is determined by the string prefix
before the first period in the file name / paula_id of each annotation layer.
In order to manually determine the visualizer and the display name for each
namespace in each corpus, the resolver table (`resolver_vis_map`) in the database must be edited.

The columns in the table can be filled out as follows:
Column | Description
-------|------------
corpus | determines the corpora for which the instruction is valid (null values apply to all corpora)
namespace | pecifies relevant namespace which triggers the visualization
element | determines if a `node` or an `edge` should carry the relevant annotation for triggering the visualization
vis_type | determines the visualizer module used from the short name of the visualizer, valid values for the ANNIS standard installation can be found in @ref admin-configure-vislist
display_name | determines the heading that is shown for each visualizer in the interface
visibility | determines the visibility state of the visualizer. Valid values are explained in @ref admin-configure-visibility
order | determines the order in which visualizers are rendered in the interface (low to high)
mappings | provides additional parameters for some visualizations, see the specific visualizer documentation for valid values

## Visibility column ## {#admin-configure-visibility}

These are the valid values for the `visibility` column in the `resolver_vis_map` table
- `permanent` - the visualizer is shown and not closeable
- `hidden` - the visualizer is hidden, but expandable
- `visible` - the visualizer is visible and closeable
- `preloaded` - behaviour is the same as `hidden`, but the visualizer starts to be rendered in the background
- `removed` - this visualizer is disabled, useful to deactivate a visualizer that is always shown (like [KWIC](@ref annis.visualizers.component.KWICPanel))

## Visualizer list ## {#admin-configure-vislist}



Short name| Description | Link to documentation | Screenshot
----------|-------------|------------|-----------
`kwic` | shows word in a specific context and also token annotations. | [KWICVisualizer](@ref annis.visualizers.component.kwic.KWICVisualizer) | ![kwic](kwic_vis.png)
`tree` | constituent syntax tree | [TigerTreeVisualizer](@ref annis.visualizers.component.tree.TigerTreeVisualizer) | ![tree](tiger_tree_vis.png)
`grid` | annotation grid, with annotations spanning multiple tokens | [GridVisualizer](@ref annis.visualizers.component.grid.GridVisualizer)  | ![grid](grid_vis.png)
`grid_tree` | a grid visualizing hierarchical tree annotations as ordered grid layers | [GridTreeVisualizer](@ref annis.visualizers.iframe.gridtree.GridTreeVisualizer ) | ![grid_tree](grid_tree_vis.png)
`discourse` | a view of the entire text of a document, possibly with interactivecoreference links. | [CorefVisualizer](@ref annis.visualizers.iframe.CorefVisualizer) | ![discourse](discourse_vis.png)
`coref` | a textual view of the match, possibly with interactivecoreference links. | [MatchCorefVisualizer](@ref annis.visualizers.iframe.MatchCorefVisualizer)| ...
`arch_dependency` | dependency tree with labeled arches between tokens | [VakyarthaDependencyTree](@ref annis.visualizers.iframe.dependency.VakyarthaDependencyTree) | ![arch_dependency](arch_dependency_vis.png)
`ordered_dependency` | arrow based dependency visualization for corpora with dependencies between non terminal nodes |[ProielRegularDependencyTree](@ref annis.visualizers.component.dependency.ProielRegularDependencyTree) | ![ordered_dependency](ordered_dependency_vis.png)
`hierarchical_dependency` | unordered vertical tree of dependent tokens | [ProielDependecyTree](@ref annis.visualizers.component.dependency.ProielDependecyTree) | ![hierarchical_dependency](hierarchical_dependency_vis.png)
`dot_vis` | a debug view of the annotation graph | [DotGraphVisualizer](@ref annis.visualizers.component.graph.DotGraphVisualizer) | ![dot_vis](graph_vis.png)
`video` | a linked video file | [VideoVisualizer](@ref annis.visualizers.component.VideoVisualizer) | ![video](video.png)
`audio` | a linked audio file | [AudioVisualizer](@ref annis.visualizers.component.AudioVisualizer) | ![audio](audio.png)
`rst` and `rstdoc` | imitates the RST-diagrams from the [RST-Tool](http://www.wagsoft.com/RSTTool/) for a match or complete document| [RST](@ref annis.visualizers.component.rst.RST)/[RSTFull](@ref annis.visualizers.component.rst.RSTFull) | ![rst](rst_vis.png)
`raw_text` | simple and default visualizer for the document browser, shows the content of the text.tab file for a specific document.| [RawTextVisualizer](@ref annis.visualizers.component.RawTextVisualizer) | ![raw text](raw_text_vis.png)
`visjs`	 | A view of the salt model of context, which contains key words. Note, spanning nodes will be divided into classes according to their annotation keys. All spanning nodes belonging to the same class appear in the same level. | [VisJs](@ref annis.visualizers.component.visjs.VisJs)|![salt model of context](visjs.png)
`visjsdoc` | A view of the salt model of the entire document, which contains key words.  Note, spanning nodes will be divided into classes according to their annotation keys. All spanning nodes belonging to the same class appear in the same level. | [VisJsDoc](@ref annis.visualizers.component.visjs.VisJsDoc) | ![salt model of document](visjsdoc.png)



## Visualizations with Software Requirements ## {#admin-configure-visibility}

Some ANNIS visualizers require additional software, depending on whether or
not they render graphics as an image directly in Java or not. At present, three
visualizations require an installation of the freely available software GraphViz
(http://www.graphviz.org/): [ordered_dependency](@ref annis.visualizers.component.dependency.ProielRegularDependencyTree),
[hierarchical_dependency](@ref annis.visualizers.component.dependency.ProielDependecyTree) and
the general [dot](@ref annis.visualizers.component.graph.DebugVisualizer) visualization. To use these, install GraphViz on the server (or
your local machine for Kickstarter) and make sure it is available in your system
path (check this by calling e.g. the program `dot` on the command line).

## Configure filter options for visjs and visjsdoc visualizers
 
How already mentioned `visjs`  visualizes the salt model of context, which contains key words.
Furthermore it provides a possibility to filter nodes (spans and structures) and/or relations (pointing, spanning, dominance)  according to their salt annotations. The latter can be filtered also according to their type. Therefore the filter parameter must be set by an 
entry in the `mappings` column of the `resolver_vis_map` table. Note, tokens will always be displayed.

Use `annos: anno_name1, anno_name2, ... ,anno_nameN` to specify nodes, which must be displayed. All spanning nodes and structure nodes, which contain listed annotation names will be displayed. The relations can be filtered according to their type and annotation. 
Use `pointingRelAnnos: anno_name1, anno_name2, ..., anno_nameN` to filter pointing relations,
`spanningRelAnnos: anno_name1, anno_name2, ..., anno_nameN` to filter spanning relations,
`dominanceRelAnnos: anno_name1, anno_name2, ..., anno_nameN` to filter dominance relations. `anno_nameX` can be written as cleartext or as a regular expression. 

If no filter parameter defined for an object class, all objects of this class will be displayed. For instance, with the following filter parameter string   `annos: cat, Topic, ambiguity; dominanceRelationAnnos: func`   we would obtain all tokens, all spanning nodes and structure nodes, whose list of annotation keys contains at least one of the stings  `cat`,  `Topic` or `ambiguity`, all dominance relations, whose list of annotation keys contains string `func`, all spanning relations and all pointing relations.

`visjsdoc` visualizes the entire salt model of document, which contains key words. The filter parameter can be set in the same manner as for `visjs`.


# Document Browser # {#document-visualizer}

The default configuration for the document browser is stored in the
`conf/document_browser.json` file. It can be overwritten by a custom
the `document_browser.json` file placed in the `ExtData` directory of
a corpus.

## automatic switch on/off ##

The ANNIS importer tries to detect an artificial token
segmentation. If the text.tab contains only artificial token (which
means there are only white spaces) the document browser is
disabled. In the case there exists a `document_browser.json` file
which configures the document browser it will never be disabled by
ANNIS. Also if in the `corpus.properties` the `browse-documents`
properties is set to true, the document browser will stay active.

## custom visualizer and sorting ## {#custom-visualizer-and-sorting}

It is also possible to use a custom visualizer for browsing a whole
document. The configuration is in JSON-Syntax file named
document_browser.json, which can be add to the ExtData directory of
each corpus.


\code{.json}
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

\endcode

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



# Web fonts # {#web-fonts}

The configuration of web fonts is placed within an instance file (see
[configuration of instances](@ref admin-configure-instance)). Thus a
web font is applied to a specific instance. If you not want to define
an extra instance, it is possible to add the font configuration to the
default.json file in the *instance* directory. If no *instance*
directory or default.json file exists, create it. Add a property
**font** to the config with the following parameters:

\code{.json}
{
 ...

 "font" :
 {
   "name" : "foo",
   "url": "https://example.com/foo.css",
   "size": "12pt" // optional
 }
}
\endcode

You must also provide a css file, which contains the `@font-face` rule
und is reachable under the defined link in the instance config:

\code{.css}

@font-face {
  font-family: 'bar';
  font-style: normal;
  font-weight: normal;
  font-size: larger;
  src:
	local('bar'),
	url(bar.woff) format('woff');
}
\endcode

Further explantation about the `@font-face` rule is availabe on the [W3C
websites](http://www.w3.org/TR/css-fonts-3/#font-face-rule).

If you need to have a different font configuration for the frequency chart
just add a `frequency-font` entry. It has the same structure as `font`.
