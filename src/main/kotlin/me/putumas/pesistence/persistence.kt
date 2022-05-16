package me.putumas.pesistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.putumas.command.ERR_MSG_CUST_NOT_FOUND
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * The object model for a customer
 *
 */
data class Customer(
    val name: String,
    val id: String,
    var address: String? = null,
    var creditRating: Int = 0,
    var reasonForUpdate: String? = null
)

const val ERR_MSG_CUST_EXISTS = "Customer with id '%s' exists"

class CustomerExistsException(errorMessage: String) : Exception(errorMessage)
class CustomerDoesNotExistException(errorMessage: String) : Exception(errorMessage)

/**
 * The available operation to manipulate the customer
 * in the storage
 * The underlying of the implementation should
 * be responsible for establishing connection
 * to database
 */
interface PersistenceManager {
    /**
     * The duplicate entry check should be implemented
     * in the underlying implementation of
     * persistence manager
     */
    @Throws(CustomerExistsException::class)
    fun insert(customer: Customer): Int

    fun get(id: String): Customer?
    fun update(customer: Customer): Int
    fun delete(id: String): Int
//    fun list(): List<Customer>
}

fun buildHikariDataSource(): HikariDataSource {
    val config = HikariConfig()
    config.driverClassName = "com.impossibl.postgres.jdbc.PGDriver"
    config.jdbcUrl = "jdbc:pgsql://testing.desktop.local:30432/creditrating"
    config.maximumPoolSize = 3
    config.isAutoCommit = false
    config.transactionIsolation = "TRANSACTION_READ_COMMITTED"
    config.username = "creditor"
    config.password = "creditor"
    config.validate()
    return HikariDataSource(config)
}


class PersistenceManagerImpl : PersistenceManager {
    var db: Database = Database.connect(datasource = buildHikariDataSource())
    override fun insert(customer: Customer): Int {
        var insertedCount = 0
        transaction(db) {
            addLogger(StdOutSqlLogger)
            insertedCount = try {
                val stmt = Customers.insert {
                    it[id] = customer.id
                    it[name] = customer.name
                }
                stmt.insertedCount
            } catch (exception: ExposedSQLException) {
                // if (exception.message!!.contains("org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException")) {
                throw CustomerExistsException(String.format(ERR_MSG_CUST_EXISTS, customer.id))
                //}
                // throw java.lang.Exception("error while inserting ${customer.id} err=${exception.stackTrace}")

            }

        }
        return insertedCount
    }

    override fun get(id: String): Customer? {
        var customer: Customer? = null
        transaction(db) {
            addLogger(StdOutSqlLogger)
            customer = Customers.select { Customers.id eq id }.limit(1).map {
                Customer(
                    id = it[Customers.id],
                    name = it[Customers.name],
                    address = it[Customers.address],
                    creditRating = it[Customers.creditRating],
                    reasonForUpdate = it[Customers.reasonForUpdate]
                )
            }.let {
                if (it.isEmpty()) throw CustomerDoesNotExistException(
                    String.format(
                        ERR_MSG_CUST_NOT_FOUND,
                        id
                    )
                ) else it.reduce { _, customer -> customer }
            }
        }
        return customer

    }

    override fun update(customer: Customer): Int {
        var affectedRecord = 0
        transaction(db) {
            addLogger(StdOutSqlLogger)
            affectedRecord = Customers.update {

                it[id] = customer.id
                it[name] = customer.name
                it[address] = customer.address
                it[creditRating] = customer.creditRating
                it[reasonForUpdate] = customer.reasonForUpdate
            }
        }
        return affectedRecord
    }

    override fun delete(id: String): Int {
        var affectedRecord = 0
        transaction(db) {
            affectedRecord = Customers.deleteWhere { Customers.id eq id }
        }
        return affectedRecord
    }

}



