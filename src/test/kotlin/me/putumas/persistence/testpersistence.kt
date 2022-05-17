package me.putumas.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.putumas.command.ERR_MSG_CUST_NOT_FOUND
import me.putumas.command.InvalidCustomerDataException
import me.putumas.pesistence.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.*

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
    companion object {
        val persistenceManager = PersistenceManagerImpl()
        var db: Database? = null
        val customerNameNominalCase = "Jack"
        val customerId = "C1234567891"
        val customerTooLongName1 =
            "ToooooooooooooooooooooooooooooooooooooooooooooLooooooooooooooooooooooooooooooooooooooooooooooooooooo"
        val customerTooLongName2 =
            "ToooooooooooooooooooooooooooooooooooooooooooooLooooooooooooooooooooooooooooooooooooooooooooooooooooong"

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

    /**
     *Clean after every test
     */
    @BeforeTest
    fun cleanup() {
        val deletedRecord = persistenceManager.delete(customerId)
        assertTrue(message = "Expecting 0 or greater got $deletedRecord") { deletedRecord >= 0 }

    }

    /**
     * This is to verify that the insert customer successfully
     * creates the new customer with correct input
     * /1 create a customer object with the id and name provided
     *  id is using the correct format, C1234567891
     *  the customer table does not have any record with the same id
     * /2 call PersistenceManager.insert
     * /3 the insert is successful, no exception returned and the integer 1
     *    is returned correctly
     * /4 verify by calling the get method
     *
     */
    @Test
    fun testInsertNominalCase() {
        val affectedRecord = persistenceManager.insert(Customer(name = customerNameNominalCase, id = customerId))
        assertEquals(1, affectedRecord)
        val existingCustomer = persistenceManager.get(id = customerId)
        assertNotNull(existingCustomer)
        assertEquals(customerId, existingCustomer.id)
        assertEquals(customerNameNominalCase, existingCustomer.name)
        assertNull(existingCustomer.address)
        assertNull(existingCustomer.reasonForUpdate)
        assertTrue(message = "expecting 0 get ${existingCustomer.creditRating}") { 0 == existingCustomer.creditRating }
    }

    /**
     * This is to verify that the insert customer will throw an exception
     * InvalidCustomerDataException with a correct message when
     * the customer with the same id exists in database is inserted
     * /1 create a customer object with the id and name provided
     *  id is using the correct format, C1234567891
     *  the customer table does not have any record with the same id
     * /2 call PersistenceManager.insert
     * /3 the insert is successful, no exception returned and the integer 1
     *    is returned correctly
     * /4 verify by calling the get method
     * /5 call the insert for the same customer id
     * /6 the InvalidCustomerDataException is thrown
     * /7 cleanup data
     */
    @Test
    fun testInsertDuplicateEntry() {
        val affectedRecord = persistenceManager.insert(Customer(name = customerNameNominalCase, id = customerId))
        assertEquals(1, affectedRecord)
        val existingCustomer = persistenceManager.get(id = customerId)
        assertNotNull(existingCustomer)
        //reinsert
        val customerExistException = assertFailsWith<CustomerExistsException> {
            persistenceManager.insert(
                Customer(
                    name = customerNameNominalCase,
                    id = customerId
                )
            )
        }
        assertEquals(String.format(ERR_MSG_CUST_EXISTS, customerId), customerExistException.message)
    }

    /**
     * Verify the behaviour of the insert if one or more
     * customer fields are invalid
     * \1 name longer than 200 chars
     * \2 address longer than 200 chars
     * \3 reason longer than 400 chars
     * \4 id longer than 11 chars
     * \5 id with incorrect format
     * \5 credit rating with negative value
     * \7 credit rating with value less than 1000
     * \8 credit rating with value greater than 2000
     */
    @ParameterizedTest(name = "{0} {1}")
    @MethodSource
    fun testInsertWithInvalidInput(customer: Customer, expectedMessage: String) {
        val exception = assertFailsWith<InvalidCustomerDataException> {
            persistenceManager.insert(
                Customer(
                    name = "$customerTooLongName1$customerTooLongName2",
                    id = customerId
                )
            )
        }
        assertEquals(ERR_MSG_CUST_FIELD_INVALID, exception.message)
    }

    @Test
    fun testCrud() {
        //get the nonexistent data
        val noDataFoundException =
            assertFailsWith<CustomerDoesNotExistException> { persistenceManager.get("C1234567891") }
        assertEquals(String.format(ERR_MSG_CUST_NOT_FOUND, "C1234567891"), noDataFoundException.message)
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
        val nodataAfterDeleted =
            assertFailsWith<CustomerDoesNotExistException> { persistenceManager.get("C1234567891") }
        assertEquals(String.format(ERR_MSG_CUST_NOT_FOUND, "C1234567891"), nodataAfterDeleted.message)
        //persistenceManager.delete("C1234567891")
        //persistenceManager.update(existingCustoer)

    }
}
