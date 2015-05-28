@ECHO off

del /F /Q work\plugins
call mvn -Dmaven.test.skip=true -DskipTests=true clean <nul
call mvnDebug hpi:run <nul
