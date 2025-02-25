package service

import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback
import configuration.QueueServiceMapper
import org.slf4j.LoggerFactory

class MessageProcessor {
    private val logger = LoggerFactory.getLogger(MessageProcessor::class.java)

    fun createQueue(channel: Channel, queue: String) {
        require(queue.isNotBlank()) { "<e91d01a2> Ошибка: Имя очереди не может быть пустым!" }
        channel.queueDeclare(queue, true, false, false, null)
        channel.basicConsume(queue, true, createDeliverCallback(channel, queue)) { _, sig ->
            logger.error("<a87c01b7> Соединение с RabbitMQ разорвано: ${sig.message}", sig)
        }
        logger.info("<8949603b> Очередь '$queue' создана.")
    }

    private fun createDeliverCallback(channel: Channel, queue: String): DeliverCallback =
        DeliverCallback { _, delivery ->
            val message = String(delivery.body)
            val replyToQueue = delivery.properties.replyTo
            runCatching {
                requireNotNull(replyToQueue) { "<9f2a4f02> Ошибка: Не указана очередь для ответа для сообщения: $message" }
                logger.info(
                    "<0b4b21bb> Получено сообщение из очереди '$queue': \n$message\n" +
                            "Ответ будет отправлен в очередь: $replyToQueue"
                )
                processedMessage(channel, replyToQueue, message, QueueServiceMapper.getQueueName(queue))
            }.onFailure { e ->
                logger.error("<ed9a5e04> Ошибка при обработке запроса из очереди '$queue': $message", e)
            }
        }

    private fun processedMessage(channel: Channel, replyToQueue: String, message: String, process: (String) -> String) {
        val response = process(message)
        sendToQueue(channel, replyToQueue, response)
    }

    private fun sendToQueue(channel: Channel, replyToQueue: String, message: String) {
        require(channel.isOpen) { "<d3fca1a8> Канал RabbitMQ закрыт, невозможно отправить сообщение!" }
        channel.queueDeclare(replyToQueue, true, false, false, null)
        channel.basicPublish("", replyToQueue, null, message.toByteArray())
        logger.info("<5794a833> Ответ отправлен в очередь '$replyToQueue': $message")
    }
}
