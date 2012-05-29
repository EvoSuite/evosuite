#!/bin/bash 

## to add -DskipTests when we put them back inb pom file
mvn package appassembler:assemble
chmod +x target/bin/EvoSuite