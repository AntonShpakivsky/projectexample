package listener

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback
import configuration.rabbitmq.RabbitMQChannelPool
import org.slf4j.LoggerFactory
import utils.configRabbitMq

class RabbitMQQueueListener(
    private val channelPool: RabbitMQChannelPool,
    queues: List<String>,
    private val processors: Map<String, (message: String) -> String>
) {
    private val logger = LoggerFactory.getLogger(RabbitMQQueueListener::class.java)

    init {
        queues.forEach { queueName ->
            val channel = channelPool.getChannel()
            createQueueDeclare(channel, queueName)
            val callback = createDeliverCallback(channel, queueName)
            channel.basicConsume(queueName, true, callback) { consumerTag ->
                logger.warn("<a8c3ad7e> Подписка на $consumerTag отменена")
            }
            logger.info("<73566265> Очередь $queueName слушается")
        }
    }

    private fun createQueueDeclare(
        channel: Channel,
        queueName: String
    ) {
        channel.queueDeclare(
            queueName,
            configRabbitMq.getBoolean("durable"),
            configRabbitMq.getBoolean("exclusive"),
            configRabbitMq.getBoolean("autoDelete"),
            null
        )
    }

    private fun createDeliverCallback(
        channel: Channel,
        queueName: String
    ) = DeliverCallback { _, delivery ->
        val message = String(delivery.body, Charsets.UTF_8)
        val correlationId = delivery.properties.correlationId
        logger.info("<3a6dc555> Получено сообщение из $queueName: $message. CorrelationId = $correlationId.")
        val replyTo = delivery.properties.replyTo
        if (replyTo == null) {
            logger.warn("")
            return@DeliverCallback
        }
        try {
            val response =
                processors[queueName]?.invoke(message)
                    ?: throw RuntimeException("<996e82e2> Для очереди $queueName не найден сервис.")
            sendResponse(replyTo, response, correlationId, channel)
        } catch (e: Throwable) {
            logger.error(
                "<e4471b63> Ошибка обработки сообщения из $queueName: ${e.message}. CorrelationId = $correlationId.",
                e
            )
        }
    }

    private fun sendResponse(
        responseQueue: String,
        response: String,
        correlationId: String?,
        channel: Channel
    ) {
        logger.info("<e4cd2dc2> Отправлен ответ в очередь $responseQueue: $response. CorrelationId = $correlationId.")
        val properties =
            AMQP.BasicProperties.Builder()
                .correlationId(correlationId)
                .build()
        channel.basicPublish("", responseQueue, properties, response.toByteArray(Charsets.UTF_8))
    }
}