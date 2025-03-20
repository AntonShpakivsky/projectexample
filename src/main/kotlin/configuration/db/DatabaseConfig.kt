package configuration.db

import com.typesafe.config.Config
import com.zaxxer.hikari.HikariDataSource
import configuration.ConnectionConfig
import org.ktorm.database.Database
import org.slf4j.LoggerFactory
import utils.ConnectionDatabaseException
import utils.configDB
import java.sql.SQLException

class DatabaseConfig(private val config: Config = configDB) : ConnectionConfig {
    private val logger = LoggerFactory.getLogger(DatabaseConfig::class.java)

    private val dataSource =
        HikariDataSource().apply {
            jdbcUrl = config.getString("url")
            driverClassName = config.getString("driver")
            username = config.getString("user")
            password = config.getString("password")
            maximumPoolSize = config.getInt("maximumPoolSize")
        }

    val database: Database = connect()

    private fun connect() = connectWithRetry()

    private fun connectWithRetry(): Database {
        logger.info("<d7383858> Начата установка соединения с Базой данных.")
        val reconnectAttempts = config.getInt("reconnectAttempt").coerceAtLeast(1)
        repeat(reconnectAttempts) { attempt ->
            logger.info("[${attempt + 1}/$reconnectAttempts] Попытка подключения к Базе данных.")
            runCatching { runConnect() }
                .onSuccess { return it }
                .onFailure { handleConnectionError(it) }
            Thread.sleep(config.getLong("delayBetweenConnectionsSec").coerceAtLeast(1) * 1000)
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