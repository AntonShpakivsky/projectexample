package configuration

import configuration.db.DatabaseConfig
import configuration.rabbitmq.RabbitMQConfig
import database.repository.ExampleRepository
import org.koin.dsl.module
import processor.ExampleProcessor
import service.example.ExampleService
import utils.configRabbitMq

val dbModule =
    module {
        single { DatabaseConfig() }
        single { ExampleRepository(get()) }
    }

val additionalModule =
    module {
//        Сервисы
        single { ExampleService(get()) }
//        Processors
        single { ExampleProcessor(get()) }
    }

val rabbitmqModule =
    module {
        single {
            RabbitMQConfig(
                reconnectAttempts = configRabbitMq.getInt("reconnectAttempts"),
                delayBetweenConnectionsMillis = configRabbitMq.getLong("delayBetweenConnectionsSec") * 1000,
                maxChannels = configRabbitMq.getInt("maxChannels"),
            )
        }
    }