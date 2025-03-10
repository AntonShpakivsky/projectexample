package configuration.rabbitmq

import com.rabbitmq.client.ConnectionFactory
import utils.configRabbitMq
import java.util.concurrent.TimeUnit

object RabbitMQConnectionFactory {
    val factory =
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
}