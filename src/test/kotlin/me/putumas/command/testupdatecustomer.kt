package me.putumas.command

import me.putumas.pesistence.Customer
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestUpdateCustomer {
    companion object {
        @JvmStatic
        fun updateCustomerInvalidInput(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    "Negative Test to verify the behaviour when reason is not present",
                    Customer(
                        name = "Jack", id = "C1234567891", address = "Bt Batok", 555
                    ),
                    ERR_MSG_CUST_REASON_NOT_PRESENT
                ),
                Arguments.of(
                    "Negative Test to verify the behaviour when both address and credit rating are missing",
                    Customer(
                        name = "Jack", id = "C1234567891", reasonForUpdate = "Customer have bought new palace"
                    ),
                    ERR_MSG_CREDIT_RATING_OR_ADDRESS_MISSING
                ),
                Arguments.of(
                    "Negative Test to verify the behaviour when id is missing",
                    Customer(
                        name = "Jack",
                        id = "",
                        reasonForUpdate = "Customer have bought new palace",
                        address = "Bt Batok",
                        creditRating = 555
                    ),
                    ERR_MSG_NAME_ID_IS_MANDATORY
                ),
                Arguments.of(
                    "Negative Test to verify the behaviour when name is missing",
                    Customer(
                        name = "",
                        id = "C1234567891",
                        reasonForUpdate = "Customer have bought new palace",
                        address = "Bt Batok",
                        creditRating = 555
                    ),
                    ERR_MSG_NAME_ID_IS_MANDATORY
                ),
                Arguments.of(
                    "Negative Test to verify the behaviour when id is incorrect format",
                    Customer(
                        name = "Jack",
                        id = "C123456789",
                        reasonForUpdate = "Customer have bought new palace",
                        address = "Bt Batok",
                        creditRating = 555
                    ),
                    String.format(ERR_MSG_CUST_ID_INVALID, "C123456789")
                )

            )
        }

        @JvmStatic
        fun updateCustomerNominalCase(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    TestCase.UPDATE_NOMINAL_CR,
                    "Nominal Test for updating credit rating",
                    Customer(
                        name = "Jack",
                        id = "C1234567891",
                        creditRating = 555,
                        reasonForUpdate = "Salary hikes incredibly"
                    )
                ),
                Arguments.of(
                    TestCase.UPDATE_NOMINAL_ADDRESS,
                    "Nominal Test for updating address",
                    Customer(
                        name = "Jack",
                        id = "C1234567891",
                        reasonForUpdate = "Customer have bought new palace",
                        address = "Blue Mountain"
                    )
                ),
                Arguments.of(
                    TestCase.UPDATE_NOMINAL_CR_ADDRESS,
                    "Nominal Test for updating address and credit rating",
                    Customer(
                        name = "Jack",
                        id = "C1234567891",
                        reasonForUpdate = "Pay increase then bought new palace",
                        address = "Bt Batok",
                        creditRating = 555
                    )
                )

            )
        }

    }

    /**
     * This is a negative test case
     * to validate
     * /1 if the reason for update is mandatory and error message is correct
     * /2 if id and name are mandatory
     * /3 if credit rating or address is mandatory
     */
    @ParameterizedTest
    @MethodSource
    fun updateCustomerInvalidInput(what: String, customer: Customer, expectedMsg: String) {

        val customerDataException = assertFailsWith<InvalidCustomerDataException> {
            updateCustomer(customer = customer)
        }
        assertEquals(
            expectedMsg,
            customerDataException.message,
            message = "Expected $expectedMsg got ${customerDataException.message}"
        )

    }

    /**
     * This is a negative test case
     * for the inconsistency between name and id that passed by the client
     * with the database record
     * The exception will be thrown
     * Test will verify the error message
     */
    @Test
    fun updateCustomerIdNameInconsistent() {
        val persistenceManagerMock = buildPersistenceMock(TestCase.UPDATE_ID_NAME_NOT_CONSISTENT)
        CommandExecutor.persistenceManager = persistenceManagerMock
        val exceptionInconsistency = assertFailsWith<InvalidCustomerDataException> {
            updateCustomer(
                Customer(
                    name = "Jack Franco",
                    id = "C1234567891",
                    reasonForUpdate = "Customer have bought new palace",
                    address = "Bt Batok",
                    creditRating = 555
                )
            )
        }
        assertEquals(
            String.format(ERR_MSG_CUST_ID_NAME_NOT_CONSISTENT, "C1234567891", "Jack"),
            exceptionInconsistency.message
        )
        verifyInteractionToMock(TestCase.UPDATE_ID_NAME_NOT_CONSISTENT, persistenceManagerMock)
    }

    /**
     * This is negative test case
     * for updating the non existing customer
     * The exception with correct message
     * will be thrown
     */
    @Test
    fun updateCustomerNotFound() {

    }

    /**
     * This is negative test case
     * when client pass customer data without any
     * changes compared to the database record
     * The following are attribute that allowed for update
     * address, credit rating
     */
    @Test
    fun updateCustomerNothingToUpdate() {

    }

    /**
     * This is nominal test case
     * when the update is done successfully
     * and persistence manager returns successful
     * \1 when both address and credit rating present
     * \2 when only address present
     * \3 when only credit rating present
     */
    @ParameterizedTest(name = "{1} with {2}")
    @MethodSource
    fun updateCustomerNominalCase(what: TestCase, label: String, customer: Customer) {
        val persistenceManagerMock = buildPersistenceMock(what)
        CommandExecutor.persistenceManager = persistenceManagerMock
        assertEquals(1, updateCustomer(customer = customer))
        verifyInteractionToMock(what, persistenceManagerMock)
    }
}