package configuration.rabbitmq

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import org.slf4j.LoggerFactory

class RabbitMQChannelPool(
    private val connection: Connection,
    private val maxChannels: Int,
) {
    private val logger = LoggerFactory.getLogger(RabbitMQChannelPool::class.java)

    private val channels = mutableListOf<Channel>()

    fun getChannel(): Channel {
        synchronized(this) {
            if (channels.size < maxChannels) {
                val newChannel = connection.createChannel()
                channels.add(newChannel)
                logger.info("<5de1988c> Создан новый канал RabbitMQ: ${newChannel.channelNumber}.")
                return newChannel
            }
            val channel = channels.random()
            logger.info("<5da93f41> Канал RabbitMQ: ${channel.channelNumber} был переиспользован.")
            return channel
        }
    }

    fun closeAll() {
        channels.forEach { it.close() }
        logger.info("<2e1906cb> Все каналы с RabbitMQ закрыты.")
    }
}
