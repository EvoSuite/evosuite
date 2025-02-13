#!/bin/sh
# execute it in evofuzz/libs

rm -rf $HOME/.m2/repository/org/apache/commons/commons-lang3/3.12.0-fix-LANG-1700
mvn deploy:deploy-file -DgroupId=org.apache.commons -DartifactId=commons-lang3 -Dversion=3.12.0-fix-LANG-1700 -Durl=file:../local-maven-repo -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=./commons-lang/target/commons-lang3-3.12.0-fix-LANG-1700.jar
