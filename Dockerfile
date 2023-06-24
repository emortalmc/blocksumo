FROM eclipse-temurin:17-jre

RUN mkdir /app
WORKDIR /app

# Add libraries required for pyroscope
RUN apt-get install wget \
    libstdc++6 libstdc++ # Add libraries required for pyroscope

COPY build/libs/*-all.jar /app/block_sumo.jar
COPY run/maps/ /app/maps/

CMD ["java", "-jar", "/app/block_sumo.jar"]
