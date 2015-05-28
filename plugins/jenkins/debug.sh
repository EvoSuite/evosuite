#!/bin/bash

rm -rf work/plugins
mvn -Dmaven.test.skip=true -DskipTests=true clean && mvnDebug hpi:run
