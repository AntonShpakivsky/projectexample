import configuration.DatabaseConfig
import configuration.RabbitMQConfig
import configuration.additionalModule
import configuration.dbModule
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
        modules(dbModule)
        modules(rabbitmqModule)
        modules(additionalModule)
    }
    val databaseConfig: DatabaseConfig by inject()
    databaseConfig.info()
    val rabbitMQConfig: RabbitMQConfig by inject()
    rabbitMQConfig.info()
}