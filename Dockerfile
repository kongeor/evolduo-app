FROM eclipse-temurin:26-jdk

COPY target/evolduo* /evolduo/app.jar

CMD ["java", "-jar", "/evolduo/app.jar"]
