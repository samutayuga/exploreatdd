package me.putumas.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.putumas.pesistence.Customer
import me.putumas.pesistence.Customers
import me.putumas.pesistence.PersistenceManagerImpl
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

fun buildHikariDataSourceInMemory(): HikariDataSource {
    val config = HikariConfig()
    config.driverClassName = "org.h2.Driver"
    config.jdbcUrl = "jdbc:h2:mem:creditrating;MODE=MYSQL"
    config.maximumPoolSize = 3
    config.isAutoCommit = false
    config.transactionIsolation = "TRANSACTION_READ_COMMITTED"
    config.username = "creditor"
    config.password = "creditor"
    config.validate()
    return HikariDataSource(config)
}

class TestPersistenceManager {


    //@Disabled
    //@Test


    companion object {
        val persistenceManager = PersistenceManagerImpl()
        var db: Database? = null

        @JvmStatic
        @BeforeAll
        fun createTable() {
            persistenceManager.db = Database.connect(datasource = buildHikariDataSourceInMemory())
            db = persistenceManager.db
            transaction() {
                addLogger(StdOutSqlLogger)
                SchemaUtils.create(Customers)
            }

        }

        @AfterAll
        @JvmStatic
        fun testDropTable() {
            transaction(db) {
                addLogger(StdOutSqlLogger)
                SchemaUtils.drop(Customers)
            }

        }

    }


    @Test
    fun testCrud() {
        val affectedRecord = persistenceManager.insert(Customer(name = "jack", id = "C1234567891"))
        assertEquals(1, affectedRecord)
        val existingCustoer = persistenceManager.get(id = "C1234567891")
        assertNotNull(existingCustoer)
        existingCustoer.address = "Bukit batok"
        existingCustoer.reasonForUpdate = "Just moved..."
        existingCustoer.creditRating = 500
        persistenceManager.update(existingCustoer)
        val existingCustoerAgain = persistenceManager.get(id = "C1234567891")
        assertEquals("Bukit batok", existingCustoerAgain!!.address)
        assertEquals("Just moved...", existingCustoerAgain.reasonForUpdate)
        assertEquals(500, existingCustoerAgain.creditRating)

        val deletedRecord = persistenceManager.delete("C1234567891")
        assertEquals(1, deletedRecord)

    }
}
