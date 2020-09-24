# Importing Corpora in the relANNIS format

Corpora in the relANNIS format can be imported into the ANNIS database. For
information on converting corpora from other formats into relANNIS, see the
[Pepper documentation](http://corpus-tools.org/pepper/userGuide.html).

## Importing a relANNIS corpus using the administration interface

When you are logged in as an administrator (automatically in desktop version), you can change to the [administration interface](../configuration/admin-web.md) and use the “Import Corpus“ tab.
On a server installation, importing a corpus will not interrupt querying other corpora that are already imported.

## Importing a corpus using the command line

Per default, the embedded graphANNIS backend will store its corpus data in the `~/.annis/v4/` folder.
You can download the latest released graphANNIS command line interface for your system (named `annis`, `annis.exe` or `annis.osx`) from the release page of graphANNIS:
<https://github.com/korpling/graphANNIS/releases/latest>.

The CLI can't access the corpus data folder while the ANNIS service is running.
Therefore, stop the running ANNIS service (e.g. with `systemctl stop annis` on a server or just closing the ANNIS desktop program) and start the graphANNIS CLI with the corpus data folder.
```bash
annis ~/.annis/v4/
```

For a list of all available commands see the [graphANNIS documentation](https://korpling.github.io/graphANNIS/docs/v0.29/cli.html).
To import a corpus, just execute the `import` command followed by a path to the corpus to import, e.g.:
```
import /home/thomas/korpora/pcc2/
``` 
The import command allows importing GraphML files and ZIP files containing more than one corpus.

