FROM openjdk:17-alpine

WORKDIR /

COPY bin/dbmate dbmate
COPY db/ db
COPY target/bubble.jar bubble.jar

EXPOSE 3000

CMD ./dbmate up && java -jar bubble.jar
