# REST API Service на чистом Spring без Boot 

user-service - web приложение на Java, использующее Spring Data JPA для взаимодействия с PostgreSQL 
Приложение поддерживает базовые операции CRUD (Create, Read, Update, Delete) над сущностью User.

При успешном изменении сущности отправляется сообщение в RabbitMQ, откуда его читает микросервис
notification-service и отправляет уведомления в асинхронном стиле
При отправке сообщения по REST возвращается статус ACCEPTED и UUID для проверки статуса отправки
также есть эндпойнт для проверки статуса отправки по UUID

### Описание
- Spring Boot 4 + Java 17
- Spring Data JPA + Hibernate + PostgreSQL + RabbitMQ + JavaMail
- Реализованы CRUD-операции для сущности User (создание, чтение, обновление, удаление) 
- Maven для управления зависимостями.

### Тесты
- MockMvc тесты интеграции контроллера + сервисного слоя, замокан JPA Repository
- Интеграционные тесты отправки email с использованием Testcontainers c MailPit

### Запуск приложения

1. Клонируйте репозиторий:
```bash
git clone https://github.com/DGorokhov123/java-rabbit-sample.git
cd java-rabbit-sample
```
2. Запустите PostgreSQL, RabbitMQ, MailPit
```bash
docker compose up -d
```
3. Соберите и запустите user-service:
```bash
./mvnw spring-boot:run -pl user-service
```
3. Соберите и запустите notification-service:
```bash
./mvnw spring-boot:run -pl notification-service
```
