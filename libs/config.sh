#!/bin/bash
SDK_NAMES=(capture-core capture-ux capture-filter-blinkid)
SDK_VERSIONS=()

for i in "${!SDK_NAMES[@]}"; do
    sdkName=${SDK_NAMES[$i]}
    SDK_VERSIONS[$i]=$(grep --max-count=1 '<version>' pom-$sdkName.xml | cut -d ">" -f 2 | cut -d '<' -f 1)
    echo $sdkName version: ${SDK_VERSIONS[$i]}
done