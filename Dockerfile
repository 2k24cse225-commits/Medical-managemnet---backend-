FROM openjdk:17

WORKDIR /app

COPY . .

RUN javac ChatbotServer.java

CMD ["java", "ChatbotServer"]
