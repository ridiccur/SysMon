# Docker

## Сборка образа
```bash
docker build -t sysmon:latest .
```

## Запуск контейнера
```bash
docker run -p 8080:8080 sysmon:latest
```

## С переменными окружения
```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  sysmon:latest
```
