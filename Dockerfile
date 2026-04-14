# Stage 1: Build the application
FROM maven:3.9.14-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Сначала копируем только POM-файлы чтобы кешировать зависимости
COPY pom.xml .
COPY common/pom.xml common/
COPY discovery-service/pom.xml discovery-service/
COPY config-service/pom.xml config-service/
COPY gateway-service/pom.xml gateway-service/
COPY user-service/pom.xml user-service/
COPY notification-service/pom.xml notification-service/
COPY tester-app/pom.xml tester-app/

# Скачиваем все зависимости
RUN --mount=type=cache,target=/root/.m2/repository mvn -B dependency:go-offline -DskipTests

# Копируем исходники
COPY common/src common/src
COPY discovery-service/src discovery-service/src
COPY config-service/src config-service/src
COPY gateway-service/src gateway-service/src
COPY user-service/src user-service/src
COPY notification-service/src notification-service/src
COPY tester-app/src tester-app/src

# Собираем проект
RUN --mount=type=cache,target=/root/.m2/repository mvn clean package -DskipTests

# Create discovery-service image
FROM eclipse-temurin:17-jre-alpine AS discovery-service
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser
WORKDIR /app
COPY --from=builder /app/discovery-service/target/*.jar app.jar
RUN chown -R appuser:appuser /app && chmod 644 app.jar
USER appuser
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]

# Create config-service image
FROM eclipse-temurin:17-jre-alpine AS config-service
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser
WORKDIR /app
COPY --from=builder /app/config-service/target/*.jar app.jar
RUN chown -R appuser:appuser /app && chmod 644 app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# Create gateway-service image
FROM eclipse-temurin:17-jre-alpine AS gateway-service
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser
WORKDIR /app
COPY --from=builder /app/gateway-service/target/*.jar app.jar
RUN chown -R appuser:appuser /app && chmod 644 app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# Create user-service image
FROM eclipse-temurin:17-jre-alpine AS user-service
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser
WORKDIR /app
COPY --from=builder /app/user-service/target/*.jar app.jar
RUN chown -R appuser:appuser /app && chmod 644 app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# Create notification-service image
FROM eclipse-temurin:17-jre-alpine AS notification-service
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser
WORKDIR /app
COPY --from=builder /app/notification-service/target/*.jar app.jar
RUN chown -R appuser:appuser /app && chmod 644 app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# Create tester-app image
FROM eclipse-temurin:17-jre-alpine AS tester-app
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser
WORKDIR /app
COPY --from=builder /app/tester-app/target/*.jar app.jar
RUN chown -R appuser:appuser /app && chmod 644 app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
