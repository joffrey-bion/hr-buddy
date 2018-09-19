FROM openjdk:8-jdk-alpine
LABEL maintainer="joffrey.bion@gmail.com"

# necessary for Spring/Tomcat temp files
VOLUME /tmp

ARG JAR_FILE
COPY ${JAR_FILE} app.jar

# security option to speedup Tomcat startup
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
