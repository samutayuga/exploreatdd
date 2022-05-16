package me.putumas.pesistence

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Customers : Table() {
    val id: Column<String> = char("id", 11)
    val name: Column<String> = varchar("name", 200)
    val address: Column<String?> = varchar("address", 200).nullable()
    val creditRating: Column<Int> = integer("creditrating").default(0)
    val reasonForUpdate: Column<String?> = varchar("reasonforupdate", 400).nullable()
    override val primaryKey = PrimaryKey(id, name = "PK_Customer_Id")
}