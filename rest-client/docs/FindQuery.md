# FindQuery

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**query** | **String** | The query to execute. |  [optional]
**queryLanguage** | [**QueryLanguage**](QueryLanguage.md) |  |  [optional]
**corpora** | [**CorpusList**](CorpusList.md) |  |  [optional]
**limit** | **Integer** | Return at most &#x60;n&#x60; matches, where &#x60;n&#x60; is the limit.  Use &#x60;null&#x60; to allow unlimited result sizes. |  [optional]
**offset** | **Integer** | Skip the &#x60;n&#x60; first results, where &#x60;n&#x60; is the offset. |  [optional]
**order** | [**OrderEnum**](#OrderEnum) |  |  [optional]

<a name="OrderEnum"></a>
## Enum: OrderEnum
Name | Value
---- | -----
NORMAL | &quot;Normal&quot;
INVERTED | &quot;Inverted&quot;
RANDOMIZED | &quot;Randomized&quot;
NOTSORTED | &quot;NotSorted&quot;
