package database.repository

import database.ExampleTable
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import java.util.UUID

class ExampleRepository(private val database: Database) {
    fun findNameById(id: UUID): String? {
        return database.from(ExampleTable)
            .select(ExampleTable.name)
            .where { ExampleTable.id eq id }
            .map { row ->
                row[ExampleTable.name] ?: throw IllegalArgumentException("<65249357> ID cannot be null")
            }
            .firstOrNull()
    }

    fun getNameById(
        id: UUID,
        onNull: () -> String
    ): String {
        return database.from(ExampleTable)
            .select(ExampleTable.name)
            .where { ExampleTable.id eq id }
            .map { row ->
                row[ExampleTable.name] ?: throw IllegalArgumentException("<7f184e33> ID cannot be null")
            }
            .firstOrNull() ?: onNull()
    }
}