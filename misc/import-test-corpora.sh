#!/bin/bash

curl -L -o pcc2.zip http://corpus-tools.org/corpora/pcc2_relANNIS.zip
curl -L -o dialog.zip https://corpus-tools.org/corpora/dialog.demo_relANNIS.zip
curl -L -o aeschylus.zip https://corpus-tools.org/corpora/Aeschylus.Persae.L1-18_relAnnis.zip
mkdir -p  $HOME/.annis/v4/
annis -c "import pcc2.zip"  $HOME/.annis/v4/
annis -c "import dialog.zip"  $HOME/.annis/v4/
annis -c "import aeschylus.zip"  $HOME/.annis/v4/