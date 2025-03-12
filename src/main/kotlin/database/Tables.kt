package database

import org.ktorm.schema.Table
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

object ExampleTable : Table<Nothing>("example") {
    val id = uuid("id").primaryKey()
    val name = varchar("name")
}