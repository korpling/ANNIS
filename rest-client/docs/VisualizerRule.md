

# VisualizerRule

A rule when to trigger a visualizer for a specific result.
## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**element** | [**ElementEnum**](#ElementEnum) | On which element type to trigger the visualizer on |  [optional]
**layer** | **String** | In which layer the element needs to be part of to trigger this visualizer.  Only relevant for edges, since only they are part of layers. If not given, elements of all layers trigger this visualization.  |  [optional]
**visType** | **String** | The abstract type of visualization, e.g. \&quot;tree\&quot;, \&quot;discourse\&quot;, \&quot;grid\&quot;, ... |  [optional]
**displayName** | **String** | A text displayed to the user describing this visualization |  [optional]
**visibility** | [**VisibilityEnum**](#VisibilityEnum) | The default display state of the visualizer before any user interaction. |  [optional]
**mappings** | [**Object**](.md) | Additional configuration given as generic map of key values to the visualizer. |  [optional]



## Enum: ElementEnum

Name | Value
---- | -----
NODE | &quot;node&quot;
EDGE | &quot;edge&quot;



## Enum: VisibilityEnum

Name | Value
---- | -----
HIDDEN | &quot;hidden&quot;
VISIBLE | &quot;visible&quot;
PERMANENT | &quot;permanent&quot;
PRELOADED | &quot;preloaded&quot;
REMOVED | &quot;removed&quot;



