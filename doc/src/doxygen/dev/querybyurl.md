Links to queries  {#dev-querybyurl}
=======================================

General
=======
If you have an external application that wants to link to a specific corpus or query in ANNIS, you can
built a special URL that will trigger the query execution. The main part of the URL is the complete host and path to your ANNIS installation e.g. `https://korpling.german.hu-berlin.de/annis3/`.

In order to execute a query, you have to append a fragment (separated with a "#") containing a set of parameters. Each parameter consists of a name, the "=" character and its value. These parameters are again separated by "&", e.g. `https://korpling.german.hu-berlin.de/annis3/#q=tok&c=pcc2`. The parameters can be optionally encoded in Base64 in order to avoid any clashes with the special characters used in the fragment. A Base64 encoded parameter always starts with an underscore (&quot;\_&quot;). E.g. if you want to specifiy the "q" parameter in Base64 encoding you name this parameter &quot;\_q&quot;.

Available parameters
====================

Name|Explanation
----|-----------
c   | A comma seperated list of corpora. If this is the only parameter instead of executing a query, the corpus will be only selected. When you use Base64 encoding the commata must be encoded as well.
q   | The AQL query to use.
l   | Limits the query to a number of matches (must be a number). *If not specified no limit is applied!*
s   | Offset ("start") for the shown matches (must be a number). Defaults to "0", so the very first matches are shown.
cl  | Optional left context (must be a number).
cr  | Optional right context (must be a number).
seg | Optional parameter to specify the segmentation on which the context should be applied.


