#docker build --build-arg JAR_FILE=target/*.jar -t mt/example -f app.Dockerfile .
FROM openjdk:11.0.12-jre
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]