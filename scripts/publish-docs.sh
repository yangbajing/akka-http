#!/bin/sh

directory=../docs/

if [ ! -d $directory ]; then
  mkdir -p $directory
fi

rm -rf ${directory}/*
cp -r ../docs-zh/target/paradox/site/main/* ${directory}/

