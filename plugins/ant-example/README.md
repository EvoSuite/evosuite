# Apache Ant + EvoSuite

The project example in this directory demonstrates how one could easily
integrate EvoSuite into an Ant build. Although there is not yet a native
EvoSuite task for the Apache Ant build system, this project examples uses the
[Java task](https://ant.apache.org/manual/Tasks/java.html) to invoke EvoSuite.

First, run `ant -f build.xml install.deps` to download all required
dependencies, i.e, latest version of JUnit, EvoSuite, and JaCoCo (for code
coverage), and then compile the project with `ant -f build.xml compile`.

Second, run `ant -f build.xml generate.tests` to invoke EvoSuite on the single
class in this project `org.ant_project_example.CharacterCounter`. The
`generate.tests` target in the `build.xml` file is configured to write the
generated test cases to `test`.

Third, run `ant -f build.xml run.tests` to execute the tests generated in the
previous step.

Fourth, run `ant -f build.xml coverage.report` to execute the generated test
cases and collect their code coverage. This task uses the
[JaCoCo](https://github.com/jacoco/jacoco) library and writes the coverage
report to `build/jacoco.report/index.html`.
