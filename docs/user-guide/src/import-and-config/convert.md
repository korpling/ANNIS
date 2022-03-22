# Converting Corpora for ANNIS using Pepper

ANNIS supports two types of formats to import:
- the legacy [relANNIS](http://korpling.github.io/ANNIS/3.6/developer-guide/annisimportformat.html) format based on the previous relational database implementation, and
- a new GraphML based native format which can be exported from ANNIS and exchanged with other tools which support GraphML (like e.g. Neo4j). 

Before a corpus can be imported into ANNIS, it has to be converted into a supported format.
The Pepper converter framework allows users to convert data from various formats including PAULA XML,
EXMARaLDA XML, TigerXML, CoNLL, RSTTool, generic XML and TreeTagger
directly into relANNIS. 
Further formats (including Tiger XML with secondary edges,
mmax2) can be converted first into PAULA XML and then into relANNIS using the
converters found on the ANNIS downloads page.

For complete information on converting corpora with Pepper see:
[http://corpus-tools.org/pepper/](http://corpus-tools.org/pepper/)
