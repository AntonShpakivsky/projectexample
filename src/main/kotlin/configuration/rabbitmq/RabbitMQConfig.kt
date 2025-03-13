package configuration.rabbitmq

import com.rabbitmq.client.ConnectionFactory
import configuration.ConnectionConfig
import configuration.ProcessorRegistry
import listener.RabbitMQQueueListener
import org.slf4j.LoggerFactory
import utils.configRabbitMq
import java.util.concurrent.TimeUnit

class RabbitMQConfig(
    reconnectAttempts: Int = 5,
    delayBetweenConnectionsMillis: Long = 10_000,
    maxChannels: Int = 10
) : ConnectionConfig {
    private val logger = LoggerFactory.getLogger(RabbitMQConfig::class.java)

    private val connectionFactory =
        ConnectionFactory().apply {
            host = configRabbitMq.getString("host")
            port = configRabbitMq.getInt("port")
            username = configRabbitMq.getString("username")
            password = configRabbitMq.getString("password")
            virtualHost = configRabbitMq.getString("virtualHost")
            isAutomaticRecoveryEnabled = configRabbitMq.getBoolean("isAutomaticRecoveryEnabled")
            networkRecoveryInterval = TimeUnit.SECONDS.toMillis(configRabbitMq.getLong("networkRecoveryInterval"))
            requestedChannelMax = configRabbitMq.getInt("requestedChannelMax")
            connectionTimeout = configRabbitMq.getInt("connectionTimeout")
            handshakeTimeout = configRabbitMq.getInt("handshakeTimeout")
        }

    private val connectionProvider =
        RabbitMQConnectionProvider(connectionFactory, reconnectAttempts, delayBetweenConnectionsMillis)

    private val channelPool = RabbitMQChannelPool(connectionProvider.connection, maxChannels)

    private val processorRegistry = ProcessorRegistry()

    private val queueListener =
        RabbitMQQueueListener(
            channelPool,
            configRabbitMq.getStringList("queue.requests"),
            processorRegistry.processors
        )

    override fun info() {
        logger.info(
            """
            Параметры подключения к RabbitMQ:
            - Host: ${connectionFactory.host}
            - Port: ${connectionFactory.port}
            - Username: ${connectionFactory.username}
            - VirtualHost: ${connectionFactory.virtualHost}
            - Connection: ${if (connectionProvider.isConnected()) "открыто" else "закрыто"}
            """.trimIndent()
        )
    }
}