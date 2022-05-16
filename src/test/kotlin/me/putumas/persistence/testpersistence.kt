package me.putumas.persistence

import me.putumas.pesistence.Customer
import me.putumas.pesistence.Customers
import me.putumas.pesistence.PersistenceManagerImpl
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Disabled
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class TestPersistenceManager {
    val persistenceManager = PersistenceManagerImpl()

    val db = persistenceManager.db

    @Disabled
    @Test
    fun createTable() {

        transaction(db) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Customers)
        }

    }

    companion object {
        //@JvmStatic

    }

    @Disabled
    @Test
    fun testDropTable() {
        transaction(db) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.drop(Customers)
        }

    }

    @BeforeTest
    fun beforeTest() {
        val deletedRecord = persistenceManager.delete("C1234567891")
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
