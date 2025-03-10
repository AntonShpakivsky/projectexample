package configuration

import configuration.db.DatabaseConfig
import configuration.rabbitmq.RabbitMQConfig
import configuration.rabbitmq.RabbitMQConnectionFactory
import configuration.rabbitmq.RabbitMQConnectionProvider
import configuration.rabbitmq.RabbitMQQueueListener
import database.repository.ExampleRepository
import org.koin.dsl.module
import utils.configRabbitMq

val dbModule =
    module {
        single { DatabaseConfig() }
        single { ExampleRepository(get()) }
    }

val rabbitmqModule =
    module {
        single {
            RabbitMQConnectionProvider(
                reconnectAttempts = configRabbitMq.getInt("reconnectAttempts"),
                delayBetweenConnectionsMillis = configRabbitMq.getLong("delayBetweenConnectionsSec") * 1000,
                factory = RabbitMQConnectionFactory.factory,
            )
        }
        single {
            RabbitMQQueueListener(
                get(),
                configRabbitMq.getStringList("queue.requests")
            )
        }
        single { RabbitMQConfig(get(), get()) }
    }

val additionalModule =
    module {
    }