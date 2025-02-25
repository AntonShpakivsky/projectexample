package utils

class ConnectionRabbitMqException(message: String) : RuntimeException(message)

class CloseConnectionRabbitMqException(message: String) : RuntimeException(message)

class ConnectionDatabaseException(message: String) : RuntimeException(message)