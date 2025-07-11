FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
WORKDIR /app
COPY target/payment-processor-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
