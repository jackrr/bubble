FROM openjdk:17-alpine

WORKDIR /

COPY target/bubble.jar bubble.jar
EXPOSE 3000

CMD java -jar bubble.jar
