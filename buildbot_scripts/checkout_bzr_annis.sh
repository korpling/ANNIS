#!/bin/bash
SUBPATH=$1
REV=`echo "$2" | sed "s/$SUBPATH,//"`
REPO=$3
OUT=$4

echo "Checking out revision $REV from repository $REPO"
bzr export -v -r $REV $OUT $REPO
