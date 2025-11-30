## Задание 16:

Использовать метрики, healthchecks и logfile

# Цель: 
Цель: реализовать production-grade мониторинг и прозрачность в приложении
Результат: приложение с применением Spring Boot Actuator

# Требования к реализации:
1. Подключить Spring Boot Actuator в приложение.
2. Включить метрики, healthchecks и logfile.
3. Реализовать свой собственный HealthCheck индикатор
4. UI для данных от Spring Boot Actuator реализовывать не нужно.
5. Опционально: переписать приложение на HATEOAS принципах с помощью Spring Data REST Repository


Основные эндпоинты метрик
Health Checks
http://localhost:8080/actuator/health - Общий статус здоровья приложения

http://localhost:8080/actuator/health/mongo - Статус MongoDB

http://localhost:8080/actuator/health/diskSpace - Статус дискового пространства

Метрики в формате Spring Boot Actuator
http://localhost:8080/actuator/metrics - Все доступные метрики

http://localhost:8080/actuator/metrics/app.books.created - Количество созданных книг

http://localhost:8080/actuator/metrics/app.books.updated - Количество обновленных книг

http://localhost:8080/actuator/metrics/app.books.deleted - Количество удаленных книг

http://localhost:8080/actuator/metrics/app.comments.added - Количество добавленных комментариев

http://localhost:8080/actuator/metrics/app.http.requests.duration - Время выполнения HTTP запросов

Метрики в формате Prometheus
http://localhost:8080/actuator/prometheus - Все метрики в формате Prometheus

JVM и системные метрики
http://localhost:8080/actuator/metrics/jvm.memory.used - Использование памяти JVM

http://localhost:8080/actuator/metrics/jvm.threads.live - Количество потоков JVM

http://localhost:8080/actuator/metrics/system.cpu.usage - Загрузка CPU

http://localhost:8080/actuator/metrics/http.server.requests - HTTP запросы

Информация о приложении
http://localhost:8080/actuator/info - Информация о приложении

http://localhost:8080/actuator/env - Переменные окружения

Кастомные метрики API
http://localhost:8080/api/v1/metrics/books - Статистика по книгам в JSON формате