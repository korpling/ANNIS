# Maximal Context Size, Context Steps and Result Page Sizes

The maximal context size of ±n tokens from each search result (for the KWIC view,
but also for other visualizations) can be set for the ANNIS service in the file
~~~
<service-home>/conf/annis-service.properties
~~~

Using the syntax, e.g. for a maximum context of 10 tokens:
~~~ini
annis.max-context=10
~~~
To configure which steps are actually shown in the front-end (up to the maximum
allowed by the service above) and the default context selected on login, edit the setting
`annis.max-context` in the `annis-service.properties`. 
By default, the context steps 1, 2, 5 or 10 tokens are available. 
To change the default step and step increment, edit the parameters `default-context=5` and `context-steps=5` respectively.

It is also possible to set context sizes individually per corpus. This is done by editing or
adding the file `corpus.properties` to the folder `ExtData` within the relANNIS corpus
folder before import. 
The names of the parameters are the same, i.e. `default-context=5` and `context-steps` and their values override the 
default values in `annis-service.properties`.

To change the available setting for the amount of hits per result page, edit the setting
`results-per-page` in `annis-service.properties` as explained above for all
corpora, or for specific corpora in `corpus.properties` within the relevant corpus.

Note that for all these setting, if multiple corpora with conflicting instructions are
selected, the interface will revert to system defaults **up to** the most restrictive settings
imposed by one of the selected corpora (i.e. if one of the selected corpora limits
context to ±5 tokens, the search will obey this limit even if other corpora and the
default setting allow more context).