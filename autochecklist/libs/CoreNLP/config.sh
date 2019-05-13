#!/bin/bash

echo "Extracting the contents of the zip file..."
unzip stanford-corenlp-full-*.zip

echo "Resolving patterns..."

dir_pattern="stanford-corenlp-full-*"
directories=( $dir_pattern )
echo "Directory: ${directories[0]}"

cd ${directories[0]}
echo "Copying jars..."
cp *.jar ../
cd ..

echo "Removing temporary directory..."
rm -rf ${directories[0]}

echo "All set!"
