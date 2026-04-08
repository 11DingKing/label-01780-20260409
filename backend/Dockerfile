# 多阶段构建
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline -DskipTests || true

COPY src ./src
RUN mvn -B package -DskipTests -q

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN useradd -m app
RUN mkdir -p /app/upload && chown app:app /app/upload
COPY --from=builder /app/target/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
