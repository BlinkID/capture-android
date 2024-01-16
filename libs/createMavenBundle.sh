#!/bin/bash

source config.sh

for i in "${!SDK_NAMES[@]}"; do
    sdkName=${SDK_NAMES[$i]}
    sdkVersion=${SDK_VERSIONS[$i]}

    tmpDir=./tmp-$sdkName
    tmpDest=$tmpDir/com/microblink/$sdkName/$sdkVersion

    mkdir -p $tmpDest
    cp $sdkName-$sdkVersion.aar $tmpDest/$sdkName-$sdkVersion.aar
    cp pom-$sdkName.xml $tmpDest/$sdkName-$sdkVersion.pom

    pushd $tmpDest > /dev/null
    md5 -q $sdkName-$sdkVersion.pom > $sdkName-$sdkVersion.pom.md5
    md5 -q $sdkName-$sdkVersion.aar > $sdkName-$sdkVersion.aar.md5
    shasum $sdkName-$sdkVersion.pom | cut -d ' ' -f 1 > $sdkName-$sdkVersion.pom.sha1
    shasum $sdkName-$sdkVersion.aar | cut -d ' ' -f 1 > $sdkName-$sdkVersion.aar.sha1
    gpg -ab $sdkName-$sdkVersion.pom
    gpg -ab $sdkName-$sdkVersion.aar
    popd > /dev/null

    (cd $tmpDir && zip -r ../$sdkName-$sdkVersion-maven-bundle.zip .)
    rm -r $tmpDir
done