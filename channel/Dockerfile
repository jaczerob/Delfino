FROM maven:3.9.6-amazoncorretto-21 AS jar

WORKDIR /opt/delfino

COPY . .

RUN mvn -f ./pom.xml clean install -DskipTests -pl '!mdrs,!elm'

FROM amazoncorretto:21

WORKDIR /opt/server

COPY --from=jar /opt/delfino/maplestory/target/*.jar ./app.jar

EXPOSE 8484

ENTRYPOINT ["java", "-jar", "./app.jar"]