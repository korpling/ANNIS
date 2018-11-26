Configure front-end web-application {#admin-configure-webapp}
==========================

[TOC]



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



