#!/bin/bash

source config.sh

for i in "${!SDK_NAMES[@]}"; do
    sdkName=${SDK_NAMES[$i]}
    sdkVersion=${SDK_VERSIONS[$i]}
    mvn install:install-file -Dfile="$sdkName-$sdkVersion.aar" -DpomFile=pom-$sdkName.xml -DcreateChecksum=true
done