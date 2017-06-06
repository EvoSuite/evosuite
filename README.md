# What is in this fork of EvoSuite?

This repository contains a re-implementation of LIPS (Linearly Independent Path based Search) in EvoSuite. Therefore, it can be used to compare LIPS with other test case generation strategies implemented in Evosuite, such as MOSA and the Whole Suite approach.


# How to run LIPS from command line

To generate a test suite with LIPS use the following command:

```java -jar evosuite.jar -generateMOSuite -Dcriterion=BRANCH -Dalgorithm=LIPS -Doutput_variables=TARGET_CLASS,criterion,algorithm,Total_Goals,Covered_Goals,Generations,Time2MaxCoverage,BranchCoverage -projectCP <classpath> -class <target class>```

where:
1. ```<classpath>``` is the classpath for the target jar, using standard Java classpath syntax
2. ```<target class>``` is the Java class under test
3. the other parameters (e.g., population size and search budget) can be set using the traditional syntax in EvoSuite (see http://www.evosuite.org/documentation/ for further details)


# How to run MOSA from command line

To generate a test suite with MOSA use the following command:

```java -jar evosuite.jar -generateMOSuite -Dcriterion=BRANCH -Dalgorithm=MOSA -Doutput_variables=TARGET_CLASS,criterion,algorithm,Total_Goals,Covered_Goals,Generations,Time2MaxCoverage,BranchCoverage -projectCP <classpath> -class <target class>```


# Building EvoSuite

EvoSuite uses [Maven](https://maven.apache.org/).

To build EvoSuite on the command line, install maven and then call

```mvn compile```

To create a binary distribution that includes all dependencies you can
use Maven as well:

```mvn package```

To build EvoSuite in Eclipse, make sure you have the [M2Eclipse](http://www.eclipse.org/m2e/) plugin installed, and import EvoSuite as Maven project. This will ensure that Eclipse uses Maven to build the project.

# Replication package

The folder ```replication_package``` contains:
1. The list of Java static methods that can be used to compare LIPS and MOSA (file "subjects.txt")
2. The JAR files containing the methods under test
