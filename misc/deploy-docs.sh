#!/bin/bash

# Used by CI to deploy the existing documentation/book/<version> directory to Github Pages

if [ -n "$GITHUB_API_KEY" ]; then
    cd "$TRAVIS_BUILD_DIR"

    echo "cloning gh-pages"
    rm -rf gh-pages
    git clone -q -b gh-pages https://$GITHUB_API_KEY@github.com/{TRAVIS_REPO_SLUG} gh-pages &>/dev/null
    cd gh-pages
   	cp -R ${TRAVIS_BUILD_DIR}/docs/*/book/* .
    git add .
    git -c user.name='travis' -c user.email='travis' commit -m "update documentation for version ${SHORT_VERSION}"
    echo "pushing to gh-pages"
    git push -q https://$GITHUB_API_KEY@github.com/{TRAVIS_REPO_SLUG} gh-pages &>/dev/null
    cd "$TRAVIS_BUILD_DIR"
else
	>&2 echo "Cannot deploy documentation because GITHUB_API_KEY environment variable is not set"
	exit 1
fi
