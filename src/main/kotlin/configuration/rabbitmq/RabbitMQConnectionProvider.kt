package configuration.rabbitmq

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import org.slf4j.LoggerFactory

class RabbitMQConnectionProvider(
    private val factory: ConnectionFactory,
    private val reconnectAttempts: Int,
    private val delayBetweenConnectionsMillis: Long
) {
    private val logger = LoggerFactory.getLogger(RabbitMQConnectionProvider::class.java)

    val connection = connect()

    private fun connect(): Connection {
        return connectWithRetry() ?: throw RuntimeException("<be8d17cb> Не удалось подключиться к RabbitMQ.")
    }

    private fun connectWithRetry(): Connection? {
        repeat(reconnectAttempts) { attempt ->
            logger.info("[${attempt + 1}/$reconnectAttempts] Попытка подключения к RabbitMQ...")
            attemptConnection()?.let { return it }
            Thread.sleep(delayBetweenConnectionsMillis)
        }
        return null
    }

    private fun attemptConnection(): Connection? {
        return runCatching { factory.newConnection() }
            .onSuccess { logger.info("Соединение с RabbitMQ установлено.") }
            .onFailure { logger.error("Ошибка подключения: ${it.localizedMessage}", it) }
            .getOrNull()
    }

    fun isConnected(): Boolean = connection.isOpen

    fun close() {
        connection.takeIf { it.isOpen }?.close()
        logger.info("<5ef15e0c> Соединение с RabbitMQ закрыто.")
    }
}