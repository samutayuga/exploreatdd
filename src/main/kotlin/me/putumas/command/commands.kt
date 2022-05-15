package me.putumas.command

import me.putumas.pesistence.Customer
import me.putumas.pesistence.CustomerDoesNotExistException
import me.putumas.pesistence.CustomerExistsException
import me.putumas.pesistence.PersistenceManager

const val ERR_MSG_NAME_ID_IS_MANDATORY = "Both 'name' and 'id' are required"
const val ERR_MSG_CUST_ID_INVALID = "%s does not follow C1234567891 format"
const val ERR_MSG_CUST_NOT_FOUND = "Customer with id '%s' does does not exist"
const val CUST_ID_REGEX = "^C[\\d]{10}$"

class InvalidCustomerDataException(errorMsg: String) : Exception(errorMsg)

/**
 * This is a singleton to hold the persistenceManager instance
 * and maintain it the whole life of application
 */
object CommandExecutor {
    var persistenceManager: PersistenceManager? = null

    /**
     * the initialization is about creating the instance
     * of persistence manager and maintain it the whole life
     * cycle of the application
     */

    init {

    }
}


/**
 * Each command inherits the abstract command where it
 * gets instance of Persistence manager
 */
//class AddCustomerCommand : AbstractCommand() {
@Throws(
    InvalidCustomerDataException::class,
    CustomerExistsException::class,

    )
fun addCustomer(customer: Customer): Int {
    //1. validate input
    //2. If it is invalid then return exception with error message
    if (customer.name.isEmpty() || customer.id.isEmpty()) {
        throw InvalidCustomerDataException(errorMsg = ERR_MSG_NAME_ID_IS_MANDATORY)
    }
    //3. Customer id has to be in a specific format, eg. CXXXXXXXXX, alphabet C uppercase followed by 10 digit number
    if (!customer.id.matches(Regex(CUST_ID_REGEX))) {
        throw InvalidCustomerDataException(errorMsg = String.format(ERR_MSG_CUST_ID_INVALID, customer.id))
    }
    //3. If it is ok then call persistence manager to insert
    return CommandExecutor.persistenceManager.let {
        it!!.insert(customer = customer)
    }
}

@Throws(
    CustomerDoesNotExistException::class,
    InvalidCustomerDataException::class
)
fun getCustomer(id: String): Customer {
    //1. validate input
    //2. If it has invalid format then return exception with error message
    if (!id.matches(Regex(CUST_ID_REGEX))) {
        throw InvalidCustomerDataException(errorMsg = String.format(ERR_MSG_CUST_ID_INVALID, id))
    }
    //3. Response from the call on persistence manager determines the response
    return CommandExecutor.persistenceManager.let { it!!.get(id) }
        ?: throw CustomerDoesNotExistException(errorMessage = String.format(ERR_MSG_CUST_NOT_FOUND, id))
}

@Throws(
    CustomerDoesNotExistException::class,
    InvalidCustomerDataException::class
)
fun updateCustomer(customer: Customer): Int {
    return 0
}