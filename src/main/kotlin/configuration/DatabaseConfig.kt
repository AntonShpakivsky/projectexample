package configuration

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.ktorm.database.Database
import org.slf4j.LoggerFactory
import utils.ConnectionDatabaseException
import java.sql.SQLException

class DatabaseConfig {
    private val logger = LoggerFactory.getLogger(DatabaseConfig::class.java)
    private val config: Config = ConfigFactory.load()

    private val reconnectAttempt = config.getInt("database.reconnectAttempt")
    private val delayBetweenConnectionsSec = config.getLong("database.delayBetweenConnectionsSec")

    private val dataSource by lazy {
        HikariDataSource().apply {
            jdbcUrl = config.getString("database.url")
            driverClassName = config.getString("database.driver")
            username = config.getString("database.user")
            password = config.getString("database.password")
            maximumPoolSize = config.getInt("database.maximumPoolSize")
        }
    }
    private val database: Database by lazy {
        runBlocking { connectWithRetry() }
    }

    private suspend fun connectWithRetry(): Database {
        logger.info("<d7383858> Начата установка соединения с Базой данных.")
        repeat(reconnectAttempt) { attempt ->
            logger.info("[${attempt + 1}/$reconnectAttempt] Попытка подключения к Базе данных.")
            runCatching { runConnect() }
                .onSuccess { return it }
                .onFailure { handleConnectionError(it) }
            delay(delayBetweenConnectionsSec * 1000)
        }
        logger.error("<bc6a7dd7> Не удалось подключиться к Базе данных после $reconnectAttempt попыток.")
        throw ConnectionDatabaseException("Не получилось выполнить подключение к БД.")
    }

    private fun runConnect(): Database =
        Database.connect(dataSource).also {
            logger.info("<c6b9e01c> Подключение к Базе данных успешно установлено.")
        }

    private fun handleConnectionError(e: Throwable) {
        val message = when (e) {
            is SQLException -> "<b99f3864> Ошибка подключения к базе данных: ${e.message}"
            else -> "<5a825e72> Неизвестная ошибка подключения к БД: ${e.message}"
        }
        logger.error(message, e)
    }

    fun info() {
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