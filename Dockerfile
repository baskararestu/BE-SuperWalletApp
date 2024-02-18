FROM adoptopenjdk/openjdk11:alpine-slim

WORKDIR /app

COPY ./target/super-wallet-backend.jar /app/super-wallet-backend.jar

CMD ["java", "-jar", "super-wallet-backend.jar"]
