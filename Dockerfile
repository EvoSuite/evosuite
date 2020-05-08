FROM maven:3-openjdk-8

ARG GIT_REPO=https://github.com/EvoSuite/evosuite.git
ARG GIT_COMMIT=master
ARG SKIP_TESTS=true

RUN adduser --system evosuite

USER evosuite
WORKDIR /home/evosuite

RUN mkdir -p /home/evosuite/results && git clone ${GIT_REPO} repo && cd repo && git checkout ${GIT_COMMIT} && mvn package -DskipTests=${SKIP_TESTS}

WORKDIR /home/evosuite/results
VOLUME /home/evosuite/results

ENTRYPOINT ["java", "-jar", "/home/evosuite/repo/master/target/evosuite-master-1.0.7-SNAPSHOT.jar"]