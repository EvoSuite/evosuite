[![Build Status](https://travis-ci.org/EvoSuite/evosuite.svg?branch=master)](https://travis-ci.org/EvoSuite/evosuite)
[![CircleCI](https://circleci.com/gh/EvoSuite/evosuite.svg?style=svg&circle-token=f00c8d84b9dcf7dae4a82438441823f3be9df090)](https://circleci.com/gh/EvoSuite/evosuite)

# What is EvoSuite?

EvoSuite automatically generates JUnit test suites for Java classes, targeting code coverage criteria such as branch coverage. It uses an evolutionary approach based on a genetic algorithm to derive test suites. To improve readability, the generated unit tests are minimized, and regression assertions that capture the current behavior of the tested classes are added to the tests.

# Using EvoSuite

There are different ways to use EvoSuite:

### EvoSuite on the command line

EvoSuite comes as an executable jar file which you can call as follows:

```java -jar evosuite.jar <options>```

To generate a test suite using EvoSuite, use the following command:

```java -jar evosuite.jar <target> [options]```

The target can be a class:

```-class <ClassName>```

or a package prefix, in which case EvoSuite tries to generate a test
suite for each class in the classpath that matches the prefix:

```-prefix <PrefixName>```

or a classpath entry, in which case EvoSuite tries to generate a test
suite for each class in the given classpath entry:

```-target <jar file or directory>```

The most important option is to set the classpath, using standard Java
classpath syntax:

```-projectCP <classpath>```

For more options, see the
[Documentation](http://www.evosuite.org/documentation/commandline/)

```java -jar evosuite.jar -help```

### EvoSuite on Docker Hub

EvoSuite has a container image available on [Docker Hub](https://hub.docker.com/r/evosuite/evosuite). You can get the container by either pulling the image:

```docker pull evosuite/evosuite:<version>```

or by manually building the image locally:

```
git clone https://github.com/EvoSuite/evosuite.git
cd evosuite
docker build -f Dockerfile.java8 . --tag evosuite/evosuite:latest-java-8
docker build -f Dockerfile.java11 . --tag evosuite/evosuite:latest-java-11
```

EvoSuite can be called as follows:

```docker run -it -u ${UID} -v ${PWD}:/evosuite evosuite/evosuite:<version>-java-<java_version> <options>```

It assumes that the project to be tested is located in the current directory the command is called from. The current directory, ```${PWD}```, is mapped to the ```/evosuite``` directory inside the container. This location is also the working directory of EvoSuite. All results will be mapped back to the directory on the host system. The ```-u ${UID}``` makes sure that the results have the same file ownership as the user initiating the command.

When EvoSuite needs to be run in the background, you can use ```-d``` instead of ```-it```.

The ```<options>``` are the same as they would be when EvoSuite is called from the command line.


#### Large-scale experiment runner

The docker image also provides a tag (```evosuite/evosuite:<version>-java-<java_version>-experiment```) to run large-scale experiments easily. You can get this image by pulling it from [Docker Hub](https://hub.docker.com/r/evosuite/evosuite):

```docker pull evosuite/evosuite:<version>-experiment```

or by manually building the image locally:

```
git clone https://github.com/EvoSuite/evosuite.git
cd evosuite
git checkout <version> # e.g. git checkout v1.1.0
docker build -f Dockerfile.java8-experiment . --tag evosuite/evosuite:<version>-java-8-experiment
docker build -f Dockerfile.java11-experiment . --tag evosuite/evosuite:<version>-java-11-experiment
```

The experiment runner can be called as follows:

```docker run -it -u ${UID} -v ${PWD}:/evosuite evosuite/evosuite:<version>-java-<java_version>-experiment [<options>] <configurations_file> <projects_file>```

and has the following options:
```
-h                       print help and exit
-m <memory>              memory limit (MB) for the EvoSuite client process (default: 2500)
-p <parallel_instances>  limit for the number of parallel executions (default: 1)
-r <rounds>              number of rounds to execute each experiment (default: 1)
-s <seeds_file>          file with the seeds for the executions of the experiment (default: SEEDS)
-t <timeout>             amount of time before EvoSuite process is killed (default: 10m)
```

It assumes that the current working directory has a folder named ```projects```, which contains a sub-directory for each project under test containing all jar files for that project. The current directory should also contain two csv files:

- One for the different configurations of the experiment, with two columns for the ```configuration_name``` and the ```user_configuration```
- One for the classes per project, with two columns for the ```project_name``` (which should be the same as the folder under ```projects```) and the ```class``` (which should be the full class path)

An example of the directory structure would be:

```
./projects/<project1_name>/<first jar file of project 1>
./projects/<project1_name>/<second jar file of project 1>
./projects/<project2_name>/<jar file of project 2>
./configurations.csv
./projects.csv
```

configurations.csv:
```
configuration_name,user_configuration
default60,-generateMOSuite -Dalgorithm=DynaMOSA -Dsearch_budget=60 -Dassertion_timeout=120 -Dminimization_timeout=120
default120,-generateMOSuite -Dalgorithm=DynaMOSA -Dsearch_budget=120 -Dassertion_timeout=120 -Dminimization_timeout=120
```

The configuration for the projectCP, class, seed, and output locations are already provided by the image.

projects.csv:
```
project_name,class
<project1_name>,com.project1.application
```

The image will put the output of the experiment in the following locations (inside the current directory):

- ```./results/<configuration_name>/<project_name>/<class_name>/logs/<round>```
- ```./results/<configuration_name>/<project_name>/<class_name>/reports/<round>/```
- ```./results/<configuration_name>/<project_name>/<class_name>/tests/<round>/```

When you run the image, it will automatically generate a SEEDS file in the current directory containing the seeds it used for the experiment. When you want to replicate the experiment, you can put that file back in the same place and instead of creating new seeds the image will now use those seeds.

When you want to set a manual class path instead of letting the script determine one for you, a file called, 'CLASSPATH' can be put inside the individual project folders where the first line is the class path for that project.

When EvoSuite needs to be run in the background, you can use ```-d``` instead of ```-it```.

### EvoSuite plugin for Eclipse

There is an experimental Eclipse plugin available using the following
update site: <http://www.evosuite.org/update>

To see what the plugin does check out the [screencast](http://www.evosuite.org/documentation/eclipse-plugin/).

### EvoSuite plugin for Maven

EvoSuite has a Maven Plugin that can be used to generate new test cases as part of the build. This has at least the following advantages:

1. Can run EvoSuite from Continuous Integration servers (eg Jenkins) with minimal configuration overheads
2. Generated tests can be put directly on the classpath of the system based on the pom.xml files
3. No need to install EvoSuite on local machine (Maven will take care of it automatically)

For more details, check the
[documentation](http://www.evosuite.org/documentation/maven-plugin/)

### EvoSuite plugin for IntelliJ

Check out the [documentation](http://www.evosuite.org/documentation/intellij-idea-plugin/).

# Getting EvoSuite

The current release of EvoSuite (main EvoSuite jar file and plugins) is available for download at <http://www.evosuite.org/downloads/>.

To access the source code, use the github repository:

```git clone https://github.com/EvoSuite/evosuite.git```


# Building EvoSuite

EvoSuite uses [Maven](https://maven.apache.org/).

To build EvoSuite on the command line, install maven and then call

```mvn compile```

To create a binary distribution that includes all dependencies you can
use Maven as well:

```mvn package```

To build EvoSuite in Eclipse, make sure you have the [M2Eclipse](http://www.eclipse.org/m2e/) plugin installed, and import EvoSuite as Maven project. This will ensure that Eclipse uses Maven to build the project.


# More Information

Usage documentation can be found at <http://www.evosuite.org/documentation/>

The developers' mailing list is hosted at <https://groups.google.com/forum/#!forum/evosuite>

EvoSuite has resulted in a number of publications, all of which are available at <http://www.evosuite.org/publications/>


