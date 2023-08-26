FROM eclipse-temurin:20-jdk

COPY target/evolduo* /evolduo/app.jar

CMD ["java", "-jar", "/evolduo/app.jar"]
