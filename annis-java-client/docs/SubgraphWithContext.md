

# SubgraphWithContext

Defines a subgraph of an annotation graph using node IDs and a context.
## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**nodeIds** | **List&lt;String&gt;** | A list of node IDs that should be part of the subgraph. |  [optional]
**segmentation** | **String** | Segmentation to use for defining the context, Set to null or omit it if tokens should be used. |  [optional]
**left** | **Integer** | Left context size. |  [optional]
**right** | **Integer** | Right context size. |  [optional]



