# Importing Corpora in the relANNIS format

Corpora in the relANNIS format can be imported into the ANNIS database. For
information on converting corpora from other formats into relANNIS, see the
[Pepper documentation](http://corpus-tools.org/pepper/userGuide.html).

## Importing a relANNIS Corpus in ANNIS Kickstarter
To import a corpus to your local Kickstarter, press the “Import Corpus” button on the
Kickstarter program window and navigate to the directory containing the relANNIS
directory of your corpus. Select this directory (but do not go into it) and press OK.
Note that you cannot import a second corpus with the same name into the system: the
first corpus must be deleted before a new one with the same name is imported.


## Importing a relANNIS Corpus into an ANNIS Server
Follow the steps described in the [installation section](installation-server.md) for importing the demo corpus GUM 
(can be downloaded from [http://corpus-tools.org/annis/corpora.html](http://corpus-tools.org/annis/corpora.html)).

Multiple corpora can be imported with `annis-admin.sh` by supplying a space-separated
list of paths to relANNIS folders after the import command:
~~~bash
bin/annis-admin.sh import path1 path2 ...
~~~

You can also use the `-o` flag to overwrite existing corpora:
~~~bash
bin/annis-admin.sh import -o path1 ...
~~~

### Disk-based corpus representation

***Warning!*** This is functionality is beta-quality at best. It is not possible to store the corpora completely on-disk, yet.

It is possible to select if a corpus should be imported as in-memory corpus or if it should prefer disk-based storage
with the `--disk-based` parameter, which can be either `true` or `false`.

~~~bash
bin/annis-admin.sh import --disk-based true path1 ...
~~~

You can also set the default value using the `prefer-disk-based` setting in the `annis-service.properties` configuration file.