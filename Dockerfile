FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY . .
RUN javac *.java

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/*.class .
EXPOSE 8080
CMD ["java", "WebServer"]
