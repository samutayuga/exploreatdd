package me.putumas.command

import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import me.putumas.pesistence.Customer
import me.putumas.pesistence.CustomerExistsException
import me.putumas.pesistence.ERR_MSG_CUST_EXISTS
import me.putumas.pesistence.PersistenceManager
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

enum class TestCase {
    INSERT_DUPLICATE_ENTRY,
    INSERT_NOMINAL_CASE,
    GET_NO_DATA,
    GET_NOMINAL,
    UPDATE_NOMINAL_CR,
    UPDATE_NOMINAL_ADDRESS,
    UPDATE_NOMINAL_CR_ADDRESS,
    UPDATE_ID_NAME_NOT_CONSISTENT
}

typealias PersistenceMocker = (TestCase) -> PersistenceManager

val persistenceMock: PersistenceMocker = {
    unmockkAll()
    when (it) {

        TestCase.INSERT_DUPLICATE_ENTRY -> {
            val pm: PersistenceManager = mockk()
            every { pm.insert(Customer("Jack", "C1234567891")) } throws (CustomerExistsException(
                String.format(
                    ERR_MSG_CUST_EXISTS, "C1234567891"
                )
            ))
            pm
        }
        TestCase.INSERT_NOMINAL_CASE -> {
            val pm: PersistenceManager = mockk()
            every { pm.insert(Customer("Jack", "C1234567891")) } returns 1
            pm
        }
        TestCase.GET_NO_DATA -> {
            val pm: PersistenceManager = mockk()
            every { pm.get("C1234567891") } returns null
            pm
        }
        TestCase.GET_NOMINAL -> {
            val pm: PersistenceManager = mockk()
            every { pm.get("C1234567891") } returns Customer("Jack", "C1234567891")
            pm
        }
        TestCase.UPDATE_NOMINAL_CR -> {
            val pm: PersistenceManager = mockk()
            every { pm.get("C1234567891") } returns Customer(
                name = "Jack",
                id = "C1234567891"
            )
            every {
                pm.update(
                    Customer(
                        name = "Jack",
                        id = "C1234567891",
                        creditRating = 555,
                        reasonForUpdate = "Salary hikes incredibly"
                    )
                )
            } returns 1
            pm
        }
        TestCase.UPDATE_NOMINAL_ADDRESS -> {
            val pm: PersistenceManager = mockk()
            every { pm.get("C1234567891") } returns Customer(
                name = "Jack",
                id = "C1234567891"
            )
            every {
                pm.update(
                    Customer(
                        name = "Jack",
                        id = "C1234567891",
                        reasonForUpdate = "Customer have bought new palace",
                        address = "Blue Mountain"
                    )
                )
            } returns 1
            pm
        }
        TestCase.UPDATE_NOMINAL_CR_ADDRESS -> {
            val pm: PersistenceManager = mockk()
            every { pm.get("C1234567891") } returns Customer(
                name = "Jack",
                id = "C1234567891"
            )
            every {
                pm.update(
                    Customer(
                        name = "Jack",
                        id = "C1234567891",
                        reasonForUpdate = "Pay increase then bought new palace",
                        address = "Bt Batok",
                        creditRating = 555
                    )
                )
            } returns 1
            pm
        }
        TestCase.UPDATE_ID_NAME_NOT_CONSISTENT -> {
            val pm: PersistenceManager = mockk()
            every { pm.get("C1234567891") } returns Customer(
                name = "Jack",
                id = "C1234567891"
            )
            pm
        }
    }
}

fun buildPersistenceMock(
    testCase: TestCase,
    transformer: (TestCase) -> PersistenceManager = persistenceMock
): PersistenceManager {
    return transformer(testCase)
}

/**
 * Test the add customer function
 */
class TestAddCustomerCommand {

    companion object {
        @JvmStatic
        fun testAddCustomerInvalidInput(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    "Negative add customer test with missing name", Customer("", "112232"),
                    ERR_MSG_NAME_ID_IS_MANDATORY
                ),
                Arguments.of(
                    "Negative add customer test with missing id", Customer("jack", ""),
                    ERR_MSG_NAME_ID_IS_MANDATORY
                ),
                Arguments.of(
                    "Negative add customer test with missing name and id", Customer("", ""),
                    ERR_MSG_NAME_ID_IS_MANDATORY
                ),
                Arguments.of(
                    "Negative add customer test with valid name but invalid id", Customer("Jack", "C1234"),
                    String.format(ERR_MSG_CUST_ID_INVALID, "C1234")
                ),
            )
        }
    }

    /**
     * This is the negative scenario with invalid input
     * Create a customer object with empty name
     * Call the addCustomer function
     * It should respond with exception and an error message accordingly
     * There are several possible negative scenarios pertaining to the
     * invalid input. eg. missing name, missing id, missing both id and name
     */
    @ParameterizedTest(name = "{0} with argument {1}")
    @MethodSource
    fun testAddCustomerInvalidInput(testLabel: String, customer: Customer, expectedMessage: String) {
        val customerMissingNameException =
            assertFailsWith<InvalidCustomerDataException>(message = "Should throw InvalidCustomerDataException") {
                addCustomer(customer = customer)
            }
        assertEquals(
            expectedMessage,
            customerMissingNameException.message,
            "Expected message is $expectedMessage got ${customerMissingNameException.message}"
        )

    }

    /**
     * This is the negative test cases where the customer with the same id
     * exists in database. The exception is thrown by the PersistenceManager layer
     * since it has direct access to database.
     */
    @Test
    fun testAddCustomerWithDuplicateEntry() {
        CommandExecutor.persistenceManager = buildPersistenceMock(testCase = TestCase.INSERT_DUPLICATE_ENTRY)
        val customerExistsException = assertFailsWith<CustomerExistsException> {
            addCustomer(Customer("Jack", "C1234567891"))
        }
        assertEquals(
            "Customer with id 'C1234567891' exists",
            customerExistsException.message,
            "Expecting message Customer with id 'C1234567891' exists got ${customerExistsException.message}"
        )

    }

    /**
     * This is the nominal test cases
     *
     */
    @Test
    fun testAddCustomerNominalCase() {
        CommandExecutor.persistenceManager = buildPersistenceMock(testCase = TestCase.INSERT_NOMINAL_CASE)
        assertEquals(
            1,
            addCustomer(Customer("Jack", "C1234567891"))
        )

    }
}