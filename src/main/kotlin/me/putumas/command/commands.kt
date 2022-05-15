package me.putumas.command

import me.putumas.pesistence.Customer
import me.putumas.pesistence.CustomerDoesNotExistException
import me.putumas.pesistence.CustomerExistsException
import me.putumas.pesistence.PersistenceManager

const val ERR_MSG_NAME_ID_IS_MANDATORY = "Both 'name' and 'id' are required"
const val ERR_MSG_CUST_ID_INVALID = "%s does not follow C1234567891 format"
const val ERR_MSG_CUST_NOT_FOUND = "Customer with id '%s' does does not exist"
const val ERR_MSG_CUST_ID_NAME_NOT_CONSISTENT = "Customer with id '%s' and name %s has different name in the system"
const val ERR_MSG_CUST_REASON_NOT_PRESENT = "Reason is mandatory for the update"
const val ERR_MSG_CREDIT_RATING_OR_ADDRESS_MISSING = "Neither credit rating nor address is provided"
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
    //1. Name and id are mandatory
    if (customer.name.isEmpty() || customer.id.isEmpty()) {
        throw InvalidCustomerDataException(errorMsg = ERR_MSG_NAME_ID_IS_MANDATORY)
    }
    //2. Check if the reason is present. If reason is not present throw exception
    if (customer.reasonForUpdate.isNullOrEmpty()) {
        throw InvalidCustomerDataException(errorMsg = ERR_MSG_CUST_REASON_NOT_PRESENT)
    }
    //3. Check if address or credit rating present
    if (!(!customer.address.isNullOrEmpty() || customer.creditRating > 0)) {
        throw InvalidCustomerDataException(errorMsg = ERR_MSG_CREDIT_RATING_OR_ADDRESS_MISSING)
    }
    //4. Check if customer with a given id exist in db. If not throw exception
    val existingCustomer = getCustomer(customer.id)
    //5. Check if customer with the same id has the same name. If not throw exception
    if (!(existingCustomer.name == customer.name && existingCustomer.id == customer.id)) {
        throw InvalidCustomerDataException(
            errorMsg = String.format(
                ERR_MSG_CUST_ID_NAME_NOT_CONSISTENT,
                existingCustomer.id,
                existingCustomer.name
            )
        )
    }
    //6. Call update customer
    return CommandExecutor.persistenceManager!!.update(customer = customer)
    //return -1
}