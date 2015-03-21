(Working in progress notes on how to use the EvoSuite Maven Plugin).


EvoSuite has a Maven Plugin that can be used to generate new test cases as part of the build.
This has at least the following advantages:
- Can run EvoSuite from Continuous Integration servers (eg Jenkins) with minimal configuration overheads
- Generated tests can be put directly on the classpath of the system based on the pom.xml files
- No need to install EvoSuite on local machine (Maven will take care of it automatically)

To enable the use of the plugin, it needs to be configured in the pom.xml of the target project.
For example:

<pluginManagement>
	<plugins>
		<plugin>
			<groupId>org.evosuite.plugins</groupId>
			<artifactId>evosuite-maven-plugin</artifactId>
			<version>${evosuiteVersion}</version>
			<executions><execution>
				<goals> <goal> prepare </goal> </goals>
				<phase> process-test-classes </phase>
			</execution></executions>
		</plugin>
    </plugins>
</pluginManagement>


where ${evosuiteVersion} specify the version to use. For example, "0.1.0":

<properties>
	<evosuiteVersion>0.1.0</evosuiteVersion>
</properties>

Note: currently EvoSuite is not hosted yet on Maven Central. It is hosted at
www.evosuite.org/m2. Such remote plugin repository needs to be added to the pom file, eg:

    <pluginRepositories>
        <pluginRepository>
            <id>EvoSuite</id>
            <name>EvoSuite Repository</name>
            <url>http://www.evosuite.org/m2</url>
        </pluginRepository>
    </pluginRepositories>

Beside configuring the plugin, there is also the need to add the EvoSuite runtime, which
is used by the generated test cases. This can be done by adding the following Maven
dependency in the pom.xml:

<dependency>
	<groupId>org.evosuite</groupId>
	<artifactId>evosuite-standalone-runtime</artifactId>
	<version>${evosuiteVersion}</version>
	<scope>test</scope>
</dependency> 


Still, dependencies and plugins are handled separately by Maven, even if on same repository.
You will also need to add this dependency repository to the pom file:

    <repositories>
        <repository>
            <id>EvoSuite</id>
            <name>EvoSuite Repository</name>
            <url>http://www.evosuite.org/m2</url>
        </repository>
    </repositories>


You also need to configure the surefire plugin to run an initializing listener for the EvoSuite tests.
This is required for when EvoSuite tests are mixed with manually written existing tests.

	<plugin>
		<groupId>org.apache.maven.plugins</groupId>
		<artifactId>maven-surefire-plugin</artifactId>
		<version>2.17</version>
		<configuration>
			<properties>
				<property>
					<name>listener</name>
					<value>org.evosuite.runtime.InitializingListener</value>
				</property>
			</properties>
		</configuration>
	</plugin>


EvoSuite generates JUnit files, so it requires JUnit on the classpath. EvoSuite does not add it
automatically as a dependency, as to avoid conflicts with different versions. We recommend to use
a recent version of JUnit, at least 4.11 or above.

<dependency>
	<groupId>junit</groupId>
	<artifactId>junit</artifactId>
	<version>4.11</version>
	<scope>test</scope>
</dependency>

-----------------------------------------------------------

The "evosuite" plugin provides the following targets:

1) "generate" ->  this is used to generate test cases with EvoSuite. Tests will be generated for
all classes in all submodules. You need to be sure the code is compiled, eg "mvn compile evosuite:generate".
This target as the following parameters:
- "memoryInMB": total amount of megabytes EvoSuite is allowed to allocate (default 800)
- "cores": total number of CPU cores EvoSuite can use (default 1)
- "timeInMinutesPerClass": how many minutes EvoSuite can spend generating tests for each class (default 2)


2) "info" -> provide info on all the generated tests so far 


3) "export" -> by default, EvoSuite creates the tests in the ".evosuite/evosuite-tests" folder.
By using "export", the generated tests will be copied over to another folder, which can
be set with the "targetFolder" option (default value is "src/test/java").
Note: if you do not export the tests into "src/test/java" with "mvn evosuite:export", then
commands like "mvn test" will not execute such tests, as their source code is not on the
build path. You can add custom source folders with "build-helper-maven-plugin" plugin, eg:

<plugin>
	<groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>1.8</version>
    <executions>
        <execution>
            <id>add-test-source</id>
            <phase>generate-test-sources</phase>
            <goals>
                <goal>add-test-source</goal>
            </goals>
            <configuration>
            	<sources>
                    <source>${customFolder}</source>
            	</sources>
            </configuration>
        </execution>
    </executions>
</plugin>

If ${customFolder} is equal to ".evosuite/evosuite-tests", then you do not need to use "evosuite:export".
(If you do, then you will get compilation errors, as each test will appear twice on the classpath).

Note: another approach is to override "<build><testSourceDirectory>" to point to ${customFolder}.
This can be useful if one wants to run "mvn test" only on the EvoSuite generated ones (eg, if having 2 different
configurations/profiles on Jenkins, one running only the existing manual tests, and the other only the EvoSuite ones).


4) "clean" -> delete _all_ data in the ".evosuite" folder, which is used to
store all the best tests generated so far.


5) "prepare" -> need to run the EvoSuite tests mixed with existing ones, eg "mvn evosuite:prepare test". 
Best to just configure the evosuite plugin to always run it, as previously explained.  

------------------

Usage example:

mvn -DmemoryInMB=2000 -Dcores=2 evosuite:generate evosuite:export  test 

This will generate tests for all classes using 2 cores and 2GB of memory, copy the generated
tests to "src/test/java" and then execute them.
Note: if the project has already some tests, those will be executed as well as part
of the regular "test" phase.


------------------

Clover issues:
if the system has been instrumented with Clover, then the generation of new tests with EvoSuite
might fail. This can happen if Clover's runtime libraries are not on the classpath.
Either you need to be sure of having all needed libraries on the classpath, or just
simply make a clean build (e.g., "mvn clean compile") before calling the EvoSuite plugin.


------------------

Requirements: the plugin needs Maven 3.1 or higher. If not, it will fail with difficult to
understand error messages. To be sure to use the right version, use the following plugin:

			<plugin>
                <inherited>true</inherited>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-enforcer-plugin</artifactId>
                <version>1.3.1</version>
                <executions>
                    <execution>
                        <id>enforce-maven-3</id>
                        <goals>
                            <goal>enforce</goal>
                        </goals>
                        <configuration>
                            <rules>
                                <requireMavenVersion>
                                    <version>3.1</version>
                                </requireMavenVersion>
                            </rules>
                            <fail>true</fail>
                        </configuration>
                    </execution>
                </executions>
            </plugin>





 