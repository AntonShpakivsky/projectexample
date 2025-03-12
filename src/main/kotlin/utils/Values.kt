package utils

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

val configRabbitMq: Config = ConfigFactory.load().getConfig("rabbitmq")

const val DEFAULT_ERROR_MESSAGE = "Сервис временно недоступен"