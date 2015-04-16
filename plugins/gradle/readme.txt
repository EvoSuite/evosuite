Trying to implement a Gradle plugin in the same way as in Maven,
but Gradle documentation/forums are very limited.
Maybe best to just have an embedded EvoSuite in the IDEs (IntelliJ, Eclipse) and
do version check for right standalone-runtime in the classpath.

Asked question on:

http://stackoverflow.com/questions/29675156/gradle-java-custom-plugin-find-url-of-a-dependency-and-recursiverly-all-the-one

Anyway, we ll mainly need Gradle for CTG on Jenkins, and so that should be done after Jenkins is done.



This module should be compiled with Maven, not Gradle, as to simplify version handling/deployment.
But first should check if works in Gradle. Afterwards, look at:

https://maven-repository.com/artifact/org.gradle/gradle-core/2.3