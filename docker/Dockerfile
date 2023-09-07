FROM maven:3.8.7-eclipse-temurin-11-focal AS build

ARG GITHUB_TOKEN

RUN mkdir /work/
COPY . /work/
WORKDIR /work/

RUN mvn -B clean install -s settings.xml -Dmaven.test.skip=true


FROM bellsoft/liberica-openjdk-debian:11
COPY --from=build /work/application/target/application-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
