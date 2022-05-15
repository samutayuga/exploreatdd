package me.putumas.pesistence

/**
 * The object model for a customer
 *
 */
data class Customer(
    val name: String,
    val id: String,
    val address: String? = null,
    val creditRating: Int? = 0,
    val reasonForUpdate: String? = null
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
//    fun update(customer: Customer)
//    fun delete(id: String)
//    fun list(): List<Customer>
}