# SysMon

SysMon — приложение для сбора и мониторинга телеметрии автобусов. Приложение принимает данные с различных датчиков (температура, давление, уровень топлива и т.п.), сохраняет их в базу PostgreSQL, выполняет проверку на аномалии, генерирует уведомления и позволяет экспортировать/импортировать данные (CSV/XLSX). Поддерживается ролевая модель (`ADMIN`/`USER`), аутентификация через JWT (cookie-based), загрузка и скачивание файлов, а также интеграция с Telegram для уведомлений.

Руководство по сборке и запуску проекта SysMon (Spring Boot + PostgreSQL).

**Требования**
- Java 17
- Maven
- Docker
- PostgreSQL

Для запуска нужно создать конфигурацию приложения и создать базовые SQL таблицы с пользователями, ролями, разрешениями и их связями

## Конфигурация: `application.yml.example`

Файл `src/main/resources/application.yml.example` содержит пример конфигурации Spring Boot для разных профилей (`dev`, `prod`). Основные поля и рекомендации:

- `spring.datasource.url`, `username`, `password` — JDBC URL и учётные данные для PostgreSQL. В примере используются переменные окружения `SPRING_DATASOURCE_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD`.
- `spring.jpa.hibernate.ddl-auto` — стратегия управления схемой БД (`update` в примере для быстрого девелопмента).
- `jwt.secret` и параметры `jwt.access`/`jwt.refresh` — секрет для JWT и параметры cookie (`access_token`, `refresh_token`, время жизни).
- `upload.path` — путь для хранения загруженных файлов.
- `telegram.bot.token` и `telegram.chat.id` — параметры для уведомлений через Telegram.
- `server.port` и `server.servlet.context-path` — порт и префикс приложения.

Использование:

- Скопируйте пример в `application.yml` или задайте значения через переменные окружения:
```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
# или задайте env vars: SPRING_DATASOURCE_URL, POSTGRES_USER, POSTGRES_PASSWORD, JWT_SECRET и т.д.
```
## SQL скрипт и схема БД: `bussensors.sql`

Файл `bussensors.sql` в корне содержит SQL-скрипт для инициализации схемы и загрузки тестовых данных:

- Пересоздаёт таблицы: `buses`, `permission`, `role`, `role_permission`, `users`, `sensor_data`.
- Заполняет справочники автобусов, прав и ролей; связывает роли с правами (`USER` имеет только чтение, `ADMIN` — полные права).
- Добавляет двух тестовых пользователей: `user` (роль USER) и `admin` (роль ADMIN). Хеши паролей уже подготовлены (BCrypt).
- Вставляет тестовые записи `sensor_data` с разными `sensor_type` и флагом `anomaly`.
- Выполняет `setval` для последовательностей, чтобы следующие вставки имели корректные id.

## Сборка приложения

**1. Локальная сборка и запуск (без Docker)**
```bash
./mvnw clean package -DskipTests
java -jar target/demo-0.0.1-SNAPSHOT.jar
```
Приложение по умолчанию слушает порт `8080`.

**2. Docker image и запуск одиночного контейнера**
Сборка образа:
```bash
docker build -t sysmon:latest .
```
Запуск контейнера (если хотите подключиться к внешнему Postgres на хосте Docker Desktop):
```bash
docker run --rm -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/yourdb \
  -e SPRING_DATASOURCE_USERNAME=youruser \
  -e SPRING_DATASOURCE_PASSWORD=yourpass \
  sysmon:latest
```

## Web UI и Swagger

Приложение поставляется с простым веб-интерфейсом и Swagger UI для изучения API:
- Веб-интерфейс: корневой путь `/` (файл `static/index.html`).
- Swagger UI: доступен по `/swagger-ui/index.html`.

Для начала использование нужно отправить `POST /api/auth/login` через Swagger (или через UI на сайте), и после авторизации использовать защищенные API.

## Использование приложения


## API Endpoints

Authentication (`/api/auth`)
- `POST /api/auth/login` — тело: `{"username":"...","password":"..."}`. Возвращает `LoginResponse` и устанавливает `access_token`/`refresh_token` в куках.
- `POST /api/auth/refresh` — обновляет access token.
- `POST /api/auth/logout` — инвалидирует токены и удаляет куки.
- `GET /api/auth/info` — возвращает данные текущего пользователя (`UserLoggedDto`). Требует аутентификации.
- `PUT /api/auth/change_password` — тело `ChangePasswordRequest`. Изменение пароля пользователя.

Buses (`/api/buses`)
- `GET /api/buses` — получить список автобусов. Доступ: `ADMIN`, `USER`.
- `POST /api/buses` — создать автобус. Доступ: `ADMIN`.
- `PUT /api/buses/{id}` — обновить автобус. Доступ: `ADMIN`.
- `DELETE /api/buses/{id}` — удалить автобус. Доступ: `ADMIN`.

Sensors (`/api/sensors`)
- `POST /api/sensors` — создать запись данных датчика. Тело: `SensorDataCreateDTO`. Доступ: `ADMIN`.
- `GET /api/sensors` — получить все данные (поддерживает `pageable` и опциональный `sensorType`). Доступ: `ADMIN`, `USER`.
- `GET /api/sensors/alerts` — получить только аномальные записи. Доступ: `ADMIN`, `USER`.
- `GET /api/sensors/{busId}` — данные для конкретного автобуса. Доступ: `ADMIN`, `USER`.
- `GET /api/sensors/history?from={ISO_DATE_TIME}&to={ISO_DATE_TIME}` — получить историю по диапазону времени. Доступ: `ADMIN`, `USER`.
- `PUT /api/sensors/{id}` — обновить запись датчика. Доступ: `ADMIN`.
- `DELETE /api/sensors/{id}` — удалить запись. Доступ: `ADMIN`.
- `POST /api/sensors/import-csv` — импорт CSV (multipart `file`). Доступ: `ADMIN`.
- `GET /api/sensors/export-csv` — экспорт всех данных в CSV (attachment). Доступ: `ADMIN`, `USER`.
- `GET /api/sensors/export-xlsx` — экспорт всех данных в XLSX (attachment). Доступ: `ADMIN`, `USER`.

Files
- `POST /upload` — загрузка CSV файла (multipart `file`). Доступ: `ADMIN`.
- `GET /download/{filename}` — скачать CSV файл из папки uploads. Доступ: `ADMIN`, `USER`.

