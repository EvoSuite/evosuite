This directory includes the updated versions of EvoSuite dependent libraries.

How to compile the updated commons-lang and deploy to local-maven-repo in the project directory:
1. cd commons-lang
2. mvn clean package -DskipTests=true
3. cd ..
4. ./deploy-to-local-repo.sh