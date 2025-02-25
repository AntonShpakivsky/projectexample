package configuration

import org.koin.dsl.module

val dbModule =
    module {
        single { DatabaseConfig() }
    }

val rabbitmqModule =
    module {
        single { RabbitMQConfig() }
    }

val additionalModule =
    module {
    }