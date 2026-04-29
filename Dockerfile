FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN ./mvnw package -DskipTests

EXPOSE 8080

CMD sh -c "java -jar target/*.jar"