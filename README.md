# REST API Service на чистом Spring без Boot 

Web приложение на Java, использующее Spring Data JPA для взаимодействия с PostgreSQL 
Приложение поддерживает базовые операции CRUD (Create, Read, Update, Delete) над сущностью User.

### Описание
- Использует Spring Data JPA + Hibernate
- База данных — PostgreSQL
- Настройка Hibernate через applicatiom.yaml + Java Config
- Реализованы CRUD-операции для сущности User (создание, чтение, обновление, удаление) 
- User состоит из полей: id, name, email, age, created_at
- REST API для взаимодействия с фронтом
- Maven для управления зависимостями.
- Логирование через logback
- @Transactional + JPA Repository для операций с базой данных.
- Обработка исключений через @RestControllerAdvice

### Тесты
- MockMvc тесты интеграции контроллера + сервисного слоя, замокан JPA Repository  

### Запуск приложения

1. Клонируйте репозиторий:
```bash
git clone https://github.com/DGorokhov123/java-hibernate-cli.git
cd java-hibernate-cli
```
2. Запустите PostgreSQL
```bash
docker compose up -d
```
3. Соберите и запустите проект:
```bash
./mvnw clean package
./mvnw exec:java -Dexec.mainClass=ru.dgorokhov.Main
```
