#!/bin/bash

# List all folders contains java_build.sh file
folders=`find . -type f -name "molecule_build.sh"`

for folder in ${folders[@]}; do
  echo 'should test ansible $folder'
  cd $folder
  ./molecule_build.sh
done
