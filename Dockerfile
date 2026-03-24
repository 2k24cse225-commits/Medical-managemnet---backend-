FROM eclipse-temurin:17

WORKDIR /app

COPY . .

RUN javac ChatbotServer.java

EXPOSE 8080

CMD ["java", "ChatbotServer"]
