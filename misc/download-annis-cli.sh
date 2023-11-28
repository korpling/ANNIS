#!/bin/bash

GRAPHANNIS_VERSION=${1:-3.0.0}

install_graphannis_cli=false

if [[ -x $HOME/.cargo/bin/annis ]]; then
    echo "Checking for graphANNIS version ${GRAPHANNIS_VERSION}"
    EXISTING_GRAPHANNIS_VERSION=`$HOME/.cargo/bin/annis --version`
    echo "Existing: ${EXISTING_GRAPHANNIS_VERSION}"
    if [ "graphANNIS CLI ${GRAPHANNIS_VERSION}" != "${EXISTING_GRAPHANNIS_VERSION}" ]; then
        install_graphannis_cli=true
    else
        echo "Using cached ${EXISTING_GRAPHANNIS_VERSION}"
        install_graphannis_cli=false
    fi
else
     install_graphannis_cli=true
fi

if [ "$install_graphannis_cli" = true ] ; then
        echo "Installing graphANNIS CLI version ${GRAPHANNIS_VERSION}"

       
        rm -f $HOME/.cargo/bin/annis
        curl -L -o annis.tar.xz https://github.com/korpling/graphANNIS/releases/download/v${GRAPHANNIS_VERSION}/graphannis-cli-x86_64-unknown-linux-gnu.tar.xz
        tar xf annis.tar.xz 
        mv graphannis-cli-x86_64-unknown-linux-gnu/annis $HOME/.cargo/bin/
fi

