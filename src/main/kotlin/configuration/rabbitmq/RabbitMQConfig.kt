package configuration.rabbitmq

import configuration.ConnectionConfig
import org.slf4j.LoggerFactory

class RabbitMQConfig(private val connectionProvider: RabbitMQConnectionProvider) : ConnectionConfig {
    private val logger = LoggerFactory.getLogger(RabbitMQConfig::class.java)

    private val factory = connectionProvider.getFactory()

    override fun info() {
        logger.info(
            """
            Параметры подключения к RabbitMQ:
            - Host: ${factory.host}
            - Port: ${factory.port}
            - Username: ${factory.username}
            - VirtualHost: ${factory.virtualHost}
            - Connection: ${if (connectionProvider.isConnected()) "открыто" else "закрыто"}
            """.trimIndent()
        )
    }
}