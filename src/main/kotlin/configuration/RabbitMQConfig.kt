package configuration

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import service.MessageProcessor
import utils.ConnectionRabbitMqException
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class RabbitMQConfig {
    private val logger = LoggerFactory.getLogger(RabbitMQConfig::class.java)
    private val config = ConfigFactory.load()

    private val reconnectAttempt = config.getInt("rabbitmq.reconnectAttempt")
    private val delayBetweenConnectionsSec = config.getLong("rabbitmq.delayBetweenConnectionsSec")

    private val factory = ConnectionFactory().apply {
        host = config.getString("rabbitmq.host")
        port = config.getInt("rabbitmq.port")
        username = config.getString("rabbitmq.username")
        password = config.getString("rabbitmq.password")
        virtualHost = config.getString("rabbitmq.virtualHost")

        isAutomaticRecoveryEnabled = config.getBoolean("rabbitmq.isAutomaticRecoveryEnabled")
        networkRecoveryInterval = TimeUnit.SECONDS.toMillis(config.getLong("rabbitmq.networkRecoveryInterval"))
        requestedChannelMax = config.getInt("rabbitmq.requestedChannelMax")

        connectionTimeout = config.getInt("rabbitmq.connectionTimeout")
        handshakeTimeout = config.getInt("rabbitmq.handshakeTimeout")
    }

    private val connection: Connection by lazy { runBlocking { connectWithRetry() } }
    private val channel: Channel by lazy {
        connection.createChannel().also {
            it.basicQos(config.getInt("rabbitmq.prefetchCountBasicQos"))
            addQueues(it)
        }
    }

    private suspend fun connectWithRetry(): Connection {
        logger.info("<2fd5d873> Начата установка соединения с RabbitMQ.")
        repeat(reconnectAttempt) { attempt ->
            logger.info("[${attempt + 1}/$reconnectAttempt] Попытка подключения к RabbitMQ.")
            runCatching { factory.newConnection() }
                .onSuccess { return it }
                .onFailure { handleConnectionError(it, attempt) }
            delay(delayBetweenConnectionsSec * 1000)
        }
        logger.error("<24f014d6> Не удалось подключиться к RabbitMQ после $reconnectAttempt попыток.")
        throw ConnectionRabbitMqException("Не получилось выполнить подключение к RabbitMQ.")
    }

    private fun handleConnectionError(e: Throwable, attempt: Int) {
        val message = when (e) {
            is IOException -> "<ed4aa5dc> [${attempt + 1}/$reconnectAttempt] Не удается установить соединение с RabbitMQ: ${e.message}"
            is TimeoutException -> "<29649961> [${attempt + 1}/$reconnectAttempt] Ошибка таймаута подключения к RabbitMQ: ${e.message}"
            else -> "<2c38989d> [${attempt + 1}/$reconnectAttempt] Неожиданная ошибка подключения к RabbitMQ: ${e.message}"
        }
        logger.error(message, e)
    }

    private fun addQueues(channel: Channel) {
        config.getStringList("rabbitmq.queue.requests").forEach { queue ->
            MessageProcessor().createQueue(channel, queue)
        }
    }

    fun info() {
        logger.info(
            """
            Параметры подключения к RabbitMQ:
            - Host: ${factory.host}
            - Port: ${factory.port}
            - Username: ${factory.username}
            - VirtualHost: ${factory.virtualHost}
            - Connection: ${if (connection.isOpen) "открыто" else "закрыто"}
            - Channel: ${if (channel.isOpen) "открыт" else "закрыт"}
            """.trimIndent()
        )
    }
}