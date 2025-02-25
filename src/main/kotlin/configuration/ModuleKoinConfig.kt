package configuration

import org.koin.dsl.module
import service.MessageProcessor

val dbModule =
    module {
        single { DatabaseConfig() }
    }

val rabbitmqModule =
    module {
        single { RabbitMQConfig(get()) }
    }

val additionalModule =
    module {
        single { MessageProcessor() }
    }