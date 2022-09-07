FROM openjdk:11
ARG JAR_FILE=/build/libs/FirebirdPostman.jar
ENV ENVIRONMENT_PROFILE=dev
ENV TZ Europe/Warsaw
ADD ${JAR_FILE} FirebirdPostman.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=${ENVIRONMENT_PROFILE}","-jar","/FirebirdPostman.jar"]
