FROM maven:3-openjdk-8

ARG GIT_REPO=https://github.com/EvoSuite/evosuite.git
ARG GIT_COMMIT=master
ARG SKIP_TESTS=true

RUN adduser --system evosuite

USER evosuite
WORKDIR /home/evosuite

RUN git clone ${GIT_REPO} evosuite && cd evosuite && git checkout ${GIT_COMMIT} && mvn package -DskipTests=${SKIP_TESTS}

ENTRYPOINT ["java", "-jar", "evosuite/master/target/evosuite-master-1.0.7-SNAPSHOT.jar"]