package me.putumas.command

import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import me.putumas.pesistence.Customer
import me.putumas.pesistence.CustomerExistsException
import me.putumas.pesistence.ERR_MSG_CUST_EXISTS
import me.putumas.pesistence.PersistenceManager

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

fun verifyInteractionToMock(
    testCase: TestCase,
    persistenceManager: PersistenceManager
) {
    when (testCase) {
        TestCase.UPDATE_NOMINAL_CR -> {
            verify(exactly = 1) { persistenceManager.get("C1234567891") }
            verify(exactly = 1) {
                persistenceManager.update(
                    Customer(
                        name = "Jack",
                        id = "C1234567891",
                        creditRating = 555,
                        reasonForUpdate = "Salary hikes incredibly"
                    )
                )
            }
        }
        TestCase.UPDATE_NOMINAL_ADDRESS -> {
            verify(exactly = 1) { persistenceManager.get("C1234567891") }
            verify(exactly = 1) {
                persistenceManager.update(
                    Customer(
                        name = "Jack",
                        id = "C1234567891",
                        reasonForUpdate = "Customer have bought new palace",
                        address = "Blue Mountain"
                    )
                )
            }

        }
        TestCase.UPDATE_NOMINAL_CR_ADDRESS -> {
            verify(exactly = 1) { persistenceManager.get("C1234567891") }
            verify(exactly = 1) {
                persistenceManager.update(
                    Customer(
                        name = "Jack",
                        id = "C1234567891",
                        reasonForUpdate = "Pay increase then bought new palace",
                        address = "Bt Batok",
                        creditRating = 555
                    )
                )
            }
        }
        TestCase.UPDATE_ID_NAME_NOT_CONSISTENT -> {
            verify(exactly = 1) { persistenceManager.get("C1234567891") }
            verify(exactly = 0) {
                persistenceManager.update(
                    Customer(
                        name = "Jack Franco",
                        id = "C1234567891",
                        reasonForUpdate = "Customer have bought new palace",
                        address = "Bt Batok",
                        creditRating = 555
                    )
                )
            }
        }
        else -> {

        }
    }
}