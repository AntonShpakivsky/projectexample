# Базовый шаблон проектов для vsetutshop
Укажите в файле gradle.properties в поле ```rootProject.name``` наименование своего проекта.

## Koin
Все модули указываются в файле ```ModuleKoinConfig```. Они были разделены на 3 части:
- ```dbModule``` - модуль для внедрения зависимостей, связанных с базой данных;
- ```rabbitmqModule``` - модуль для внедрения зависимостей, связанных с брокером сообщений RabbitMq;
- ```additionalModule``` - модуль для внедрения зависимостей, которые не подходят под условия выше.

Обратите внимание, что в файле ```application.conf``` в поле``` ktor.application``` указан модуль ```ApplicationKt.configureDI``` для ktor. Подразумевалось, что именно в нем будут внедряться зависимости.
