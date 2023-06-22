FROM eclipse-temurin:20-alpine

COPY target/evolduo* /evolduo/app.jar

CMD ["java", "-jar", "/evolduo/app.jar"]
