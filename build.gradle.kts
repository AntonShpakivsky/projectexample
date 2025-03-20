import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.ktlint)
}

group = "ru.example"
version = "0.0.1"

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
}

dependencies {
//    ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.config.yaml)
//    Работа с базой данных
    implementation(libs.postgresql)
    implementation(libs.ktorm)
    implementation(libs.hikaricp)
//    RabbitMQ клиент
    implementation(libs.rabbitmq)
//    Внедрение зависимостей
    implementation(libs.koin)
//    jackson
    implementation(libs.jackson.module)
    implementation(libs.jackson.databind)
//    Логи
    implementation(libs.logback.classic)
//    Тесты
    implementation(libs.koin.test)
    testImplementation(libs.kotest.runner.junit5.jvm)
    testImplementation(libs.kotest.assertions.core.jvm)
    testImplementation(libs.mockk)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

ktlint {
    version = "1.2.1"
    debug = false // Включить debug-логи
    verbose = true // Подробный вывод в консоль
    android = false // Если проект не Android, отключаем
    outputToConsole = true // Вывод результатов в консоль
    outputColorName = "RED" // Цвет ошибок в консоли (BLUE, RED, GREEN)
    ignoreFailures = false // Останавливать сборку при ошибке форматирования
    reporters {
        reporter(ReporterType.PLAIN) // Обычный текстовый вывод
        reporter(ReporterType.JSON) // JSON формат
        reporter(ReporterType.CHECKSTYLE) // Для CI/CD (например, GitHub Actions)
    }
}