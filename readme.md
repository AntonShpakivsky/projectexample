# Базовый шаблон проектов для vsetutshop

## Настройка проекта
Перед началом работы укажите в файле [gradle.properties](gradle.properties) значение параметра ```rootProject.name```, 
соответствующее наименованию вашего проекта.

## Koin
Все модули указываются в файле [ModuleKoinConfig](src/main/kotlin/configuration/ModuleKoinConfig.kt). 
Они были разделены на три группы:
- ```dbModule``` - модуль для внедрения зависимостей, связанных с базой данных;
- ```rabbitmqModule``` - модуль для внедрения зависимостей, связанных с брокером сообщений RabbitMq;
- ```additionalModule``` - модуль для внедрения зависимостей, которые не подходят под вышеуказанные категории.

В файле [application.conf](src/main/resources/application.conf) в блоке ``` ktor.application``` в параметре ```modules```  
указан модуль ```ApplicationKt.configureDI```, в котором происходит внедрение зависимостей.

## База данных
Для хранения данных используется база **PostgreSQL**, а для взаимодействия с ней – библиотека **Ktorm**.
Конфигурация базы данных определяется в классе [DatabaseConfig](src/main/kotlin/configuration/db/DatabaseConfig.kt).
В файле [application.conf](src/main/resources/application.conf) параметры подключения расположены в блоке database:
1. _url_ - строка подключения к базе данных;
2. _driver_ - название драйвера базы данных, который используется для подключения к СУБД;
3. _user_ - имя пользователя базы данных;
4. _password_ - пароль пользователя базы данных;
5. _maximumPoolSize_ - максимальное количество одновременных соединений;
6. _reconnectAttempt_ - число попыток повторного подключения;
7. _delayBetweenConnectionsSec_ - задержка между попытками подключения (в секундах).

## Брокер сообщений
Для обработки запросов используется брокер сообщений **RabbitMQ**.
Конфигурация брокера находится в классах:
* [RabbitMQChannelPool](src/main/kotlin/configuration/rabbitmq/RabbitMQChannelPool.kt);
* [RabbitMQConfig](src/main/kotlin/configuration/rabbitmq/RabbitMQConfig.kt);
* [RabbitMQConnectionProvider](src/main/kotlin/configuration/rabbitmq/RabbitMQConnectionProvider.kt).

Настройка слушателей: [RabbitMQQueueListener](src/main/kotlin/listener/RabbitMQQueueListener.kt).

В файле [application.conf](src/main/resources/application.conf) параметры настройки RabbitMQ находятся в блоке ```rabbitmq```:
1. host - хост (IP-адрес или доменное имя);
2. port - порт, на котором работает RabbitMQ. По умолчанию RabbitMQ использует порт 5672 для AMQP и 5671 для AMQP с SSL;
3. username - имя пользователя;
4. password - пароль пользователя;
5. virtualHost - виртуальный хост (Virtual Host) в RabbitMQ. Виртуальные хосты используются для изоляции ресурсов (очередей, обменников и т.д.) внутри одного сервера RabbitMQ;
6. reconnectAttempts - количество попыток повторного подключения к RabbitMQ в случае разрыва соединения;
7. delayBetweenConnectionsSec - задержка в секундах между попытками повторного подключения к RabbitMQ;
8. isAutomaticRecoveryEnabled - флаг, указывающий, включено ли автоматическое восстановление соединения после сбоя;
9. networkRecoveryIntervalSec - интервал времени(в секундах) между попытками восстановления сети после сбоя;
10. requestedChannelMax - максимальное количество каналов, которые могут быть открыты на одном соединении;
11. connectionTimeout - время ожидания (в миллисекундах) установления соединения с RabbitMQ до того, как попытка подключения будет считаться неудачной;
12. handshakeTimeout - время ожидания (в миллисекундах) завершения рукопожатия (handshake) при установлении соединения с RabbitMQ;
13. prefetchCountBasicQos - количество сообщений, которые могут быть одновременно обработаны потребителем (consumer) без подтверждения (ack). Это настройка для управления QoS (Quality of Service);
14. durable - флаг, указывающий, является ли очередь устойчивой (durable). Если true, очередь сохраняется на диск и переживает перезапуск сервера RabbitMQ; 
15. exclusive - флаг, указывающий, является ли очередь эксклюзивной. Эксклюзивная очередь может быть использована только одним соединением и будет удалена при закрытии соединения; 
16. autoDelete - флаг, указывающий, будет ли очередь автоматически удалена, когда все потребители отключатся; 
17. queue - настройки очереди:
    * requests - список наименований очередей, используемых сервисом.

## Пример обработки запроса из внешней системы
Логика обработки запросов выглядит следующим образом:
1. В конкретную очередь RabbitMq поступают запросы одного типа(например, для работы с таблицей Example);
2. В конфигурационном файле [application.conf](src/main/resources/application.conf) в блоке ```rabbitmq``` в блоке ```queue``` в поле ```requests``` добавляем в список новую прослушиваемую очередь. Например, ```rabbitmq.queue.requests = ["ExampleRequest"]```;
3. Для каждой очереди требуется создать класс процессор. Задача данного класса будет выполнять парсинг запроса и определять его тип. Например, [ExampleProcessor](src/main/kotlin/processor/ExampleProcessor.kt). Данные процессоры должны быть наследниками класса [Processor](src/main/kotlin/processor/Processor.kt);
4. Каждый класс процессор определяет тип запроса и сервис, который потребуется вызвать для его обработки. Например, [ExampleService](src/main/kotlin/service/example/ExampleService.kt). Данные сервисы должны быть наследниками класса [SimpleService<T1, T2>](src/main/kotlin/service/SimpleService.kt);
5. Конкретные сервисы уже могут обращаться к внешним классам, которые могут достать различные данные для обработки и ответа на запрос. Например, в примере есть класс [ExampleRepository](src/main/kotlin/database/repository/ExampleRepository.kt), который обращается к базе данных за данными.

Моменты, на которые стоит обратить внимание:
* Каждый процессор и сервис требуется зарегистрировать в файле [ModuleKoinConfig](src/main/kotlin/configuration/ModuleKoinConfig.kt) в блоке ```additionalModule```. Класс репозитория для взаимодействия с базой данных тоже требуется зарегистрировать(если он есть), но уже в блоке ```dbModule```.
* Каждый процессор требуется добавить в классе [ProcessorRegistry](src/main/kotlin/configuration/ProcessorRegistry.kt) для того, что бы он был сопоставлен с очередью.
* В классе [ProcessorRegistry](src/main/kotlin/configuration/ProcessorRegistry.kt) в словаре processors ключ должен быть наименованием очереди.
* Опционально. В процессоре обработать ошибку в общем и вернуть ответ.
* Опционально. Называть процессор по имени очереди с добавлением постсуффиком Processor. Называть сервис по типу запроса с добавлением постсуффиком Service. 

