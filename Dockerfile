FROM openjdk:18

COPY target/evolduo* /evolduo/app.jar

CMD ["java", "-jar", "/evolduo/app.jar"]