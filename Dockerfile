FROM eclipse-temurin:17-jre

WORKDIR /app

COPY . .

EXPOSE 8080

CMD ["java", "WebServer"]
