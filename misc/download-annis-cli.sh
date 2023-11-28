#!/bin/bash

GRAPHANNIS_VERSION=${1:-2.2.0}

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

        if [[ "$OS_NAME" == "linux" ]];
        then
            rm -f $HOME/.cargo/bin/annis
            curl -L -o annis.tar.xz https://github.com/korpling/graphANNIS/releases/download/v${GRAPHANNIS_VERSION}/graphannis-cli-x86_64-unknown-linux-gnu.tar.xz
            tar xf annis.tar.xz -C $HOME/.cargo/bin/
        elif [[ "$OS_NAME" == "osx" ]];
        then
            rm -f $HOME/.cargo/bin/annis
            curl -L -o annis.tar.xz https://github.com/korpling/graphANNIS/releases/download/v${GRAPHANNIS_VERSION}/graphannis-cli-x86_64-apple-darwin.tar.xz
            tar xf annis.tar.xz -C $HOME/.cargo/bin/
        elif [[ "$OS_NAME" == "windows" ]];
        then
            del /s /q $HOME/.cargo/bin/annis.exe
            curl -L -o annis.zip https://github.com/korpling/graphANNIS/releases/download/v${GRAPHANNIS_VERSION}/graphannis-cli-x86_64-pc-windows-msvc.zip
            Expand-Archive -Path annis.zip -DestinationPath $HOME/.cargo/bin/annis.exe
            
        else
            >&2 echo "Unknown value \"${OS_NAME}\" for environment variable OS_NAME"
        	exit 1
        fi
fi

