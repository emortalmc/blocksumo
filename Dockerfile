FROM --platform=$TARGETPLATFORM azul/zulu-openjdk:21-jre

RUN mkdir /app
WORKDIR /app

# Download packages
RUN apt-get update && apt-get install -y wget

COPY build/libs/*-all.jar /app/blocksumo.jar
COPY run/maps /app/maps

ENTRYPOINT ["java"]
CMD ["-jar", "/app/blocksumo.jar"]
