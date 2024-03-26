#!/bin/bash

curl -L -o pcc2.zip https://corpus-tools.org/corpora/pcc2_v7_graphml.zip
curl -L -o dialog.zip https://corpus-tools.org/corpora/dialog.demo_relANNIS.zip
curl -L -o aeschylus.zip https://corpus-tools.org/corpora/Aeschylus.Persae.L1-18_relAnnis.zip
curl -L -o shenoute.zip https://corpus-tools.org/corpora/shenoute.a22_ANNIS.zip 
curl -L -o parallelsample.zip https://corpus-tools.org/corpora/parallel.sample_relAnnis.zip
mkdir -p  $HOME/.annis/v4/
export PATH=$PATH:$HOME/.cargo/bin
annis $HOME/.annis/v4/ -c "import pcc2.zip"
annis $HOME/.annis/v4/ -c "import dialog.zip"
annis $HOME/.annis/v4/ -c "import aeschylus.zip"
annis $HOME/.annis/v4/ -c "import shenoute.zip"
annis $HOME/.annis/v4/ -c "import parallelsample.zip"
