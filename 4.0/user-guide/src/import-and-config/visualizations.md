# Configuring Visualizations

By default, ANNIS displays all search results in the Key Word in Context (KWIC)
view in the "Query Result" tab, though in some cases you may wish to turn off this
visualization (specifically dialog corpora, see below). Further visualizations, such as
syntax trees or grid views, are displayed by default based on the following namespaces 
(both nodes an edges can have namespaces):

| Element | Namespace | Visualizer      |
| ------- | --------- | --------------- |
| node    | tiger     | tree visualizer |
| node    | exmaralda | grid visualizer |
| node    | mmax      | grid visualizer |
| edge    | mmax      | discourse view  |

In these cases the namespaces are usually taken from the source format in which the
corpus was generated, and carried over into relANNIS during the conversion. It is also
possible to use other namespaces, most easily when working with PAULA XML. In
PAULA XML, the namespace is determined by the string prefix before the first period
in the file name / paula_id of each annotation layer (for more information, see the
[PAULA XML documentation](http://www.sfb632.uni-potsdam.de/en/paula.html)).
Data converted from EXMARaLDA can also optionally use speaker names as
namespaces. For other formats and namespaces, see the SaltNPepper documentation of
the appropriate format module.

In order to manually determine the visualizer and the display name for each namespace
in each corpus, the `corpus-config.toml` file for this corpus must be edited. 
For relANNIS corpus, this can be done by editing the relANNIS file `resolver_vis_map.annis` before import (see [the old user guide for instructions](http://korpling.github.io/ANNIS/3.6/user-guide/visualizations.html)).
For GraphML based corpora you can edit the `corpus-config.toml` data attribute that is added as annotation to the graph.
In both cases, you can edit the `corpus-config.toml` in the corpus data directory after import.

As with the example queries, visualizer entries are added as table arrays to the configuration file.
Each rule has the table name `[[visualizers]]`.
The order of the entries in the file also determines the order in the result view.

```toml
[[visualizers]]
element = "edge"
layer = "dep"
vis_type = "arch_dependency"
display_name = "dependencies (Stanford)"
visibility = "hidden"

[[visualizers]]
element = "edge"
layer = "ud"
vis_type = "arch_dependency"
display_name = "dependencies (UD)"
visibility = "hidden"

[[visualizers]]
element = "node"
layer = "const"
vis_type = "tree"
display_name = "constituents (tree)"
visibility = "hidden"

[visualizers.mappings]
# These are the mappings for the "constituents (tree)" visualizer entry
edge_anno_ns = "const"
edge_key = "func"
edge_type = "edge"
node_key = "cat"

[[visualizers]]
element = "node"
layer = "ref"
vis_type = "grid"
display_name = "referents (grid)"
visibility = "hidden"

[visualizers.mappings]
# These are the mappings for the "referents (grid)" visualizer entry
annos = "/ref::coref_val/,/ref::entity/,/ref::infstat/"
escape_html = "false"
```

The key value pairs for each visualizer rule can be
filled out as follows:
- *layer* specifies the relevant node or edge layer (also called namespace) which triggers the
visualization
- *element* determines if a `node` or an `edge` should carry the relevant annotation
for triggering the visualization
- *vis_type* determines the visualizer module used, see the [list of visualizations](visualizations-list.md) for possible values
- *display_name* determines the heading that is shown for each visualizer in the interface
- *visibility* is optional and can be set to:
    - *hidden* - the default setting: the visualizer is not shown, but can be
expanded by clicking on its plus symbol.
    - *permanent* - always shown, not closable
    - *visible* - shown initially, but closable by clicking on its minus symbol.
    - *removed* - not shown; this can be used to hide the KWIC visualization in corpora which require a grid by default (e.g. dialogue corpora)
    - *preloaded* - like hidden, but actually rendered in the background even
    before its plus symbol is clicked. This is useful for multimedia player
    visualizations, as the player can be invoked and a file may be loaded
    before the user prompts the playing action.
- *[visualizers.mappings]* provides additional parameters for some visualizations: a `[visualizer.mappings]` table must be added after the last key value pair of the corresponding `[[visualizers]]` entry.
  Each key corresponds to a visualizer specific parameter names (see the [list of visualizations](visualizations-list.md) for parameters for a specific visualizer).