# Job

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**jobType** | [**JobTypeEnum**](#JobTypeEnum) |  |  [optional]
**status** | [**StatusEnum**](#StatusEnum) |  |  [optional]
**messages** | **List&lt;String&gt;** |  |  [optional]

<a name="JobTypeEnum"></a>
## Enum: JobTypeEnum
Name | Value
---- | -----
IMPORT | &quot;Import&quot;
EXPORT | &quot;Export&quot;

<a name="StatusEnum"></a>
## Enum: StatusEnum
Name | Value
---- | -----
RUNNING | &quot;Running&quot;
FAILED | &quot;Failed&quot;
FINISHED | &quot;Finished&quot;
