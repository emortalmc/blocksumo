FROM eclipse-temurin:17-jre-alpine

RUN mkdir /app
WORKDIR /app

COPY build/libs/*-all.jar /app/block_sumo.jar
COPY run/maps/ /app/maps/

CMD ["java", "-jar", "/app/block_sumo.jar"]
