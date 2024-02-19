FROM adoptopenjdk/openjdk11:alpine-slim

WORKDIR /app

COPY target/super-wallet-0.0.1-SNAPSHOT.jar /app/super-wallet-0.0.1-SNAPSHOT.jar

CMD ["java", "-jar", "/app/super-wallet-0.0.1-SNAPSHOT.jar"]
