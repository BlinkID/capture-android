#!/bin/bash
version=1.2.0

mvn install:install-file -Dfile="capture-core-$version.aar" -DpomFile=pom-capture-core.xml -DcreateChecksum=true
mvn install:install-file -Dfile="capture-ux-$version.aar" -DpomFile=pom-capture-ux.xml -DcreateChecksum=true