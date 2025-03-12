package configuration.rabbitmq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback
import org.slf4j.LoggerFactory
import processor.ExampleProcessor
import utils.configRabbitMq

class RabbitMQQueueListener(
    private val connectionProvider: RabbitMQConnectionProvider,
    queues: List<String>,
    private val exampleProcessor: ExampleProcessor,
) {
    private val logger = LoggerFactory.getLogger(RabbitMQQueueListener::class.java)

    private val serviceHandlers = mapOf<String, (message: String) -> String>(
        "ExampleRequest" to { message -> exampleProcessor.process(message) },
    )

    private val channel: Channel by lazy {
        connectionProvider.connection.createChannel()
    }

    fun isChannelOpen(): Boolean = channel.isOpen

    init {
        queues.forEach { queueName ->
            createQueueDeclare(queueName)
            val callback = createDeliverCallback(queueName)
            channel.basicConsume(queueName, true, callback) {
                consumerTag -> logger.warn("<a8c3ad7e> Подписка на $consumerTag отменена")
            }
            logger.info("<73566265> Очередь $queueName слушается")
        }
    }

    private fun createQueueDeclare(queueName: String) {
        channel.queueDeclare(
            queueName,
            configRabbitMq.getBoolean("durable"),
            configRabbitMq.getBoolean("exclusive"),
            configRabbitMq.getBoolean("autoDelete"),
            null,
        )
    }

    private fun createDeliverCallback(queueName: String) =
        DeliverCallback { _, delivery ->
            val message = String(delivery.body, Charsets.UTF_8)
            logger.info("<3a6dc555> Получено сообщение из $queueName: $message")
            try {
                val response = serviceHandlers[queueName]?.invoke(message)
                    ?: throw RuntimeException("<996e82e2> Для очереди $queueName не найден сервис.")
                delivery.properties.replyTo?.let {
                    sendResponse(it, response, delivery.properties.correlationId)
                }
            } catch (e: Throwable) {
                logger.error("<e4471b63> Ошибка обработки сообщения из $queueName: ${e.message}", e)
            }
        }

    private fun sendResponse(responseQueue: String, response: String, correlationId: String?) {
        val properties = AMQP.BasicProperties.Builder()
            .correlationId(correlationId)
            .build()
        channel.basicPublish("", responseQueue, properties, response.toByteArray(Charsets.UTF_8))
        logger.info("<e4cd2dc2> Отправлен ответ в очередь $responseQueue: $response")
    }
}