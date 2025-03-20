package configuration

import configuration.db.DatabaseConfig
import configuration.rabbitmq.RabbitMQConfig
import database.repository.ExampleRepository
import org.koin.dsl.module
import org.ktorm.database.Database
import processor.ExampleProcessor
import service.example.ExampleService
import utils.configDB
import utils.configRabbitMq

val dbModule =
    module {
        single {
            DatabaseConfig(config = configDB)
        }
        single<Database> { get<DatabaseConfig>().database }
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
            )
        }
    }