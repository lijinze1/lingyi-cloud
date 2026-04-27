FROM eclipse-temurin:17-jre
WORKDIR /app
ARG JAR_FILE=target/lingyi-gateway-server-1.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
