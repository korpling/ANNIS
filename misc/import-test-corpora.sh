#!/bin/bash

curl -L -o pcc2.zip http://corpus-tools.org/corpora/pcc2_relANNIS.zip
curl -L -o dialog.zip https://corpus-tools.org/corpora/dialog.demo_relANNIS.zip
curl -L -o aeschylus.zip https://corpus-tools.org/corpora/Aeschylus.Persae.L1-18_relAnnis.zip
curl -L -o shenoute.zip https://corpus-tools.org/corpora/shenoute.a22_ANNIS.zip 
mkdir -p  $HOME/.annis/v4/
annis $HOME/.annis/v4/ -c "import pcc2.zip"
annis $HOME/.annis/v4/ -c "import dialog.zip"
annis $HOME/.annis/v4/ -c "import aeschylus.zip"
annis $HOME/.annis/v4/ -c "import shenoute.zip"
