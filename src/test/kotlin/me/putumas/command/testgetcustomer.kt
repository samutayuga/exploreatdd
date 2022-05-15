package me.putumas.command

import me.putumas.pesistence.Customer
import me.putumas.pesistence.CustomerDoesNotExistException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestGetCustomer {

    @Test
    fun getCustomerTestNoDataFound() {
        CommandExecutor.persistenceManager = buildPersistenceMock(testCase = TestCase.GET_NO_DATA)
        val notExistsException = assertFailsWith<CustomerDoesNotExistException> { getCustomer("C1234567891") }
        assertEquals(String.format(ERR_MSG_CUST_NOT_FOUND, "C1234567891"), notExistsException.message)
    }

    @Test
    fun getCustomerTestIdInvalid() {
        //CommandExecutor.persistenceManager = buildPersistenceMock(testCase = TestCase.G)
        val notExistsException = assertFailsWith<InvalidCustomerDataException> { getCustomer("C12345678") }
        assertEquals(String.format(ERR_MSG_CUST_ID_INVALID, "C12345678"), notExistsException.message)
    }

    @Test
    fun getCustomerTestNominalCase() {
        CommandExecutor.persistenceManager = buildPersistenceMock(testCase = TestCase.GET_NOMINAL)
        assertEquals(Customer("Jack", "C1234567891"), getCustomer("C1234567891"))
    }
}