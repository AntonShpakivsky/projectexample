package configuration.db

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariDataSource
import configuration.ConnectionConfig
import org.ktorm.database.Database
import org.slf4j.LoggerFactory
import utils.ConnectionDatabaseException
import java.sql.SQLException

class DatabaseConfig(
    private val reconnectAttempts: Int = 5,
    private val delayBetweenConnectionsMillis: Long = 10_000,
) : ConnectionConfig {
    private val logger = LoggerFactory.getLogger(DatabaseConfig::class.java)
    private val config: Config = ConfigFactory.load()

    private val dataSource = HikariDataSource().apply {
        jdbcUrl = config.getString("database.url")
        driverClassName = config.getString("database.driver")
        username = config.getString("database.user")
        password = config.getString("database.password")
        maximumPoolSize = config.getInt("database.maximumPoolSize")
    }

    val database = connect()

    private fun connect(): Database {
        return connectWithRetry()
    }

    private fun connectWithRetry(): Database {
        logger.info("<d7383858> Начата установка соединения с Базой данных.")
        repeat(reconnectAttempts) { attempt ->
            logger.info("[${attempt + 1}/$reconnectAttempts] Попытка подключения к Базе данных.")
            runCatching { runConnect() }
                .onSuccess { return it }
                .onFailure { handleConnectionError(it) }
            Thread.sleep(delayBetweenConnectionsMillis)
        }
        logger.error("<bc6a7dd7> Не удалось подключиться к Базе данных после $reconnectAttempts попыток.")
        throw ConnectionDatabaseException("Не получилось выполнить подключение к БД.")
    }

    private fun runConnect(): Database =
        Database.connect(dataSource).also {
            logger.info("<c6b9e01c> Подключение к Базе данных успешно установлено.")
        }

    private fun handleConnectionError(e: Throwable) {
        val message =
            when (e) {
                is SQLException -> "<b99f3864> Ошибка подключения к базе данных: ${e.message}"
                else -> "<5a825e72> Неизвестная ошибка подключения к БД: ${e.message}"
            }
        logger.error(message, e)
    }

    override fun info() {
        logger.info(
            """
            Параметры подключения к базе данных:
            - Url: ${database.url}
            - Name: ${database.name}
            - Dialect: ${database.dialect}
            - ProductName: ${database.productName}
            """.trimIndent()
        )
    }
}