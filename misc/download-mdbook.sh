#!/bin/bash

MDBOOK_VERSION=${1:-0.3.1}

install_mdbook=false

if [[ -x $HOME/.cargo/bin/mdbook ]]; then
    echo "Checking for mdBook version ${MDBOOK_VERSION}"
    EXISTING_MDBOOK_VERSION=`$HOME/.cargo/bin/mdbook --version`
    echo "Existing: ${EXISTING_MDBOOK_VERSION}"
    if [ "mdbook v${MDBOOK_VERSION}" != "${EXISTING_MDBOOK_VERSION}" ]; then
        install_mdbook=true
    else
        echo "Using cached ${EXISTING_MDBOOK_VERSION}"
        install_mdbook=false
    fi
else
     install_mdbook=true
fi

if [ "$install_mdbook" = true ] ; then
        echo "Installing mdBook version ${MDBOOK_VERSION}"

        if [[ "$TRAVIS_OS_NAME" == "linux" ]];
        then
            rm -f mdbook.tar.gz
            curl -L -o mdbook.tar.gz https://github.com/rust-lang/mdBook/releases/download/v${MDBOOK_VERSION}/mdbook-v${MDBOOK_VERSION}-x86_64-unknown-linux-gnu.tar.gz
            tar -C $HOME/.cargo/bin/ -zxf mdbook.tar.gz
        elif [[ "$TRAVIS_OS_NAME" == "osx" ]];
        then
            rm -f mdbook.tar.gz
            curl -L -o mdbook.tar.gz https://github.com/rust-lang/mdBook/releases/download/v${MDBOOK_VERSION}/mdbook-v${MDBOOK_VERSION}-x86_64-apple-darwin.tar.gz
            tar -C $HOME/.cargo/bin/ -zxf mdbook.tar.gz
        elif [[ "$TRAVIS_OS_NAME" == "windows" ]];
        then
            del /s /q mdbook.tar.gz
            curl -L -o mdbook.zip https://github.com/rust-lang/mdBook/releases/download/v${MDBOOK_VERSION}/mdBook-v${MDBOOK_VERSION}-x86_64-pc-windows-msvc.zip
            unzip -o -d $HOME/.cargo/bin/ mdbook.zip
        else
            >&2 echo "Unknown value \"${TRAVIS_OS_NAME}\" for environment variable TRAVIS_OS_NAME"
        	exit 1
        fi
fi

