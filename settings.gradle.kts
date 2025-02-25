//  Настройка имени проекта происходит через файл gradle.properties в поле rootProject.name
//  Сделано для того, что бы имя проекта указывалось в едином месте проекта.
val projectName =
    settings.extra["rootProject.name"] as? String
        ?: throw Throwable("In the gradle.properties file, you need to set the project name to rootProject.name")
rootProject.name = projectName

pluginManagement {
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}