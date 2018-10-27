#!/bin/bash

# List all folders contains java_build.sh file
folders=`find . -type f -name "java_build.sh"`

for folder in ${folders[@]}; do
  echo 'should test java $folder'
  cd $folder
  ./java_build.sh
done
