Embed ANNIS visualization {#embed}
==========

[TOC]

Introduction {#embed-intro}
========

It is possible to embed visualizations of ANNIS into another website or
to share links to ANNIS visualizations.
This is achieved by a special sub website of ANNIS which has the URL
\code
<annis-base-url>/embeddedvis/<visualizer-short-name>
\endcode
The possible values for the short name are the same as the ones listed in the \ref admin-configure-vis chapter.
Additional parameters are used to configure how the visualization should be displayed.

Parameters {#embed-params}
==========

These are URL query parameters which define the visualizer configuration.
Any parameter which is not mentioned here will be treated as visualizer mapping.

embedded_salt {#embed-param-salt}
---------------

An URL from where to fetch to content from.
The content must be in the Salt XMI intermediate format.
This can be a dynamic query (like a REST call to the [subgraph-function](@ref annis.service.QueryService#subgraph) of the service) or a link to a static file.

embedded_interface {#embed-param-interface}
---------------

URL back to the ANNIS instance from which this visualization was generated.
When given this will trigger a link to appear in the output which links back to this URL.
The URL should contain all the information (e.g. via fragment parameters) to display
the query and the selected match.

embedded_match {#embed-param-match}
---------------

A plain text match definition (as produced by the REST [find-function](@ref annis.service.QueryService#find)).
Example:
\code
salt:/pcc2/11299/#tok_1 tiger::pos::salt:/pcc2/11299/#tok_2
\endcode

embedded_instance {#embed-param-instance}
---------------

Name of an ANNIS sub-instance that should be used.
See \ref admin-configure-instance for more information.

embedded_ns {#embed-param-namespace}
---------------

Namespace which is "triggering" the visualization (see the \ref admin-configure-vis chapter)

embedded_base {#embed-param-base}
---------------

Segmentation base text to use.
Some visualizers like [KWIC](@ref annis.visualizers.component.kwic.KWICVisualizer) allow to show only a certain base text,
this parameter controls which one.

Examples {#embed-examples}
=======

In this example https://korpling.german.hu-berlin.de/annis3-snapshot/ is the base URL for the ANNIS user interface.
When entering the URL

\code
https://korpling.german.hu-berlin.de/annis3-snapshot/embeddedvis/grid?
embedded_ns=tei&
embedded_instance=&
embedded_salt=http%3A%2F%2Flocalhost%3A5713%2Fannis%2Fquery%2Fsearch%2Fsubgraph%3Fmatch%3Dtei%3A%3Asic%3A%3Asalt%3A%2FGUM%2FGUM_whow_languages%2F%2523sic_487%26left%3D5%26right%3D5&
embedded_interface=https://korpling.german.hu-berlin.de/annis3-snapshot/%23_q%3Dc2lj%26_c%3DR1VN%26cl%3D5%26cr%3D5%26s%3D10%26l%3D10%26m%3D12
\endcode
the following web page is [shown](https://korpling.german.hu-berlin.de/annis3-snapshot/embeddedvis/grid?embedded_ns=tei&embedded_instance=&embedded_salt=http%3A%2F%2Flocalhost%3A5713%2Fannis%2Fquery%2Fsearch%2Fsubgraph%3Fmatch%3Dtei%3A%3Asic%3A%3Asalt%3A%2FGUM%2FGUM_whow_languages%2F%2523sic_487%26left%3D5%26right%3D5&embedded_interface=https://korpling.german.hu-berlin.de/annis3-snapshot/%23_q%3Dc2lj%26_c%3DR1VN%26cl%3D5%26cr%3D5%26s%3D10%26l%3D10%26m%3D12):
![Embed example 1](embed_example1.png)

The namespace is "tei", the instance is the default one (empty name).
There is a dynamic URL to the REST web service running at `localhost` (the service must be reachable by the web server, not the client)
and a back-link to the interface is given.
All parameters must be URL encoded (especially the ones that are URLs by theirself).

Instead of having only a subgraph with for a single match the following example shows a [complete document](https://korpling.german.hu-berlin.de/annis3-snapshot/embeddedvis/htmldoc?embedded_instance=scriptorium2&embedded_match=Abraham::norm::salt:/abraham.our.father/Abraham.XL93-94_merge/%23norm_15&embedded_salt=http%3A%2F%2Flocalhost%3A5713%2Fannis%2Fquery%2Fgraph%2Fabraham.our.father%2FAbraham.XL93-94_merge%3Ffilternodeanno%3Dcb%2CAbraham%3A%3Anorm%2Cpb_xml_id%2Clb%2Chi_rend&embedded_interface=https://korpling.german.hu-berlin.de/annis3-snapshot/scriptorium2%23_q%3Dbm9ybT0i4rKb4rKf4rKp4rKn4rKJIg%26_c%3DYWJyYWhhbS5vdXIuZmF0aGVy%26cl%3D5%26cr%3D5%26s%3D0%26l%3D10%26_seg%3Dd29yZA%26m%3D0&embedded_base=word&config=dipl):
\code
https://korpling.german.hu-berlin.de/annis3-snapshot/embeddedvis/htmldoc?
embedded_instance=scriptorium2&
embedded_match=Abraham::norm::salt:/abraham.our.father/Abraham.XL93-94_merge/%23norm_15&
embedded_salt=http%3A%2F%2Flocalhost%3A5713%2Fannis%2Fquery%2Fgraph%2Fabraham.our.father%2FAbraham.XL93-94_merge%3Ffilternodeanno%3Dcb%2CAbraham%3A%3Anorm%2Cpb_xml_id%2Clb%2Chi_rend&
embedded_interface=https://korpling.german.hu-berlin.de/annis3-snapshot/scriptorium2%23_q%3Dbm9ybT0i4rKb4rKf4rKp4rKn4rKJIg%26_c%3DYWJyYWhhbS5vdXIuZmF0aGVy%26cl%3D5%26cr%3D5%26s%3D0%26l%3D10%26_seg%3Dd29yZA%26m%3D0&
embedded_base=word&
config=dipl
\endcode
`embedded_salt` refers to the [graph function](@ref annis.service.QueryService.graph) and this example uses the configuration from the "scriptorium2" instance.
The additional parameter `config` is a mapping parameter of the `htmldoc` visualizer.
![Embed example 2](embed_example2.png)
