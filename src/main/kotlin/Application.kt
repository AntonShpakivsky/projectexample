import configuration.additionalModule
import configuration.db.DatabaseConfig
import configuration.dbModule
import configuration.rabbitmq.RabbitMQConfig
import configuration.rabbitmqModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
}

fun Application.configureDI() {
    install(Koin) {
        modules(dbModule, additionalModule, rabbitmqModule)
    }
    val databaseConfig: DatabaseConfig by inject()
    val rabbitMQConfig: RabbitMQConfig by inject()
    databaseConfig.info()
    rabbitMQConfig.info()
}