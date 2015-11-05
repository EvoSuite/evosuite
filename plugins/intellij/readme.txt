The IntelliJ plugin for EvoSuite is not built as part of Maven compilation.
It needs to be manually compiled.
To do so, the intellij.iml project file needs to be opened from an IDE instance of IntelliJ,
and then build the plugin from there (eg, right-click on project panel and choose option to make
a deploy release, which should generate a jar file for the plugin).


Note: the IntelliJ plugin has _NO_ compilation dependency on EvoSuite. To use EvoSuite, it does
call the maven plugin from a spawn process, eg "mvn evosuite:generate".
Unless there are changes in the maven plugin options, there is no need to make a new IntelliJ
release even when new EvoSuite releases are made.


Motivation: the generated test cases should be part of the build, as they might be added to the SUT's
repository and run on a remote continuous integration server.
An IDE plugin should use what configured in the pom.xml (assuming the SUT is built in Maven), or
other configuration files used to build and run the tests of the SUT (eg ANT or Gradle).
Reading and parsing those configuration files (eg pom.xml) would be far too complicated, so it is much easier
to just let the build process to generate the test cases (eg with the Maven plugin for EvoSuite).




