#!/bin/bash

curl -L -o pcc2.zip https://corpus-tools.org/corpora/pcc2_v7_graphml.zip
curl -L -o dialog.zip https://corpus-tools.org/corpora/dialog.demo_relANNIS.zip
curl -L -o aeschylus.zip https://corpus-tools.org/corpora/Aeschylus.Persae.L1-18_relAnnis.zip
curl -L -o shenoute.zip https://corpus-tools.org/corpora/shenoute.a22_ANNIS.zip 
mkdir -p  $HOME/.annis/v4/
annis $HOME/.annis/v4/ -c 'set-disk-based on' -c "import pcc2.zip"
annis $HOME/.annis/v4/ -c 'set-disk-based on' -c "import dialog.zip"
annis $HOME/.annis/v4/ -c 'set-disk-based on' -c "import aeschylus.zip"
annis $HOME/.annis/v4/ -c 'set-disk-based on' -c "import shenoute.zip"
