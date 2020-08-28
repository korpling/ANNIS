#!/bin/bash

GRAPHANNIS_VERSION=${1:-0.29.2}

install_graphannis_cli=false

if [[ -x $HOME/.cargo/bin/annis ]]; then
    echo "Checking for graphANNIS version ${GRAPHANNIS_VERSION}"
    EXISTING_GRAPHANNIS_VERSION=`$HOME/.cargo/bin/annis --version`
    echo "Existing: ${EXISTING_GRAPHANNIS_VERSION}"
    if [ "graphANNIS CLI ${GRAPHANNIS_VERSION}" != "${EXISTING_GRAPHANNIS_VERSION}" ]; then
        install_mdbook=true
    else
        echo "Using cached ${EXISTING_GRAPHANNIS_VERSION}"
        install_graphannis_cli=false
    fi
else
     install_graphannis_cli=true
fi

if [ "$install_graphannis_cli" = true ] ; then
        echo "Installing graphANNIS CLI version ${GRAPHANNIS_VERSION}"

        if [[ "$TRAVIS_OS_NAME" == "linux" ]];
        then
            rm -f $HOME/.cargo/bin/annis
            curl -L -o $HOME/.cargo/bin/annis https://github.com/korpling/graphANNIS/releases/download/v${GRAPHANNIS_VERSION}/annis
            chmod u+x $HOME/.cargo/bin/annis
        elif [[ "$TRAVIS_OS_NAME" == "osx" ]];
        then
            rm -f $HOME/.cargo/bin/annis
            curl -L -o $HOME/.cargo/bin/annis https://github.com/korpling/graphANNIS/releases/download/v${GRAPHANNIS_VERSION}/annis.osx
            chmod u+x $HOME/.cargo/bin/annis
        elif [[ "$TRAVIS_OS_NAME" == "windows" ]];
        then
            del /s /q $HOME/.cargo/bin/annis.exe
            curl -L -o $HOME/.cargo/bin/annis.exe https://github.com/korpling/graphANNIS/releases/download/v${GRAPHANNIS_VERSION}/annis.exe
        else
            >&2 echo "Unknown value \"${TRAVIS_OS_NAME}\" for environment variable TRAVIS_OS_NAME"
        	exit 1
        fi
fi

