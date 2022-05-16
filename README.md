# Test Driven Development

## What

`Project Organization`

It is a gradle based project, following the standard layout of kotlin,

`About Gradle in Nutshell`

* Script <Interface>
>All file ending with `.gradle`. Implemented in every gradle script. Build script life cycle. How to access log property
>According to [Script Interface](https://docs.gradle.org/current/javadoc/org/gradle/api/Script.html), `Script` object has delegate object attached to it
>Build script has `Project` object

* Project <Interface>
> build.gradle
Access the method in the build `Script`
```kotlin
logger.info("This is the starting of the build script")
apply {
    println("hello from ${rootProject.name} ")

}
println("This is in ${file(".")}")
//println("File tree ${fileTree(".")}")
println("File tree ${fileTree("src").asFileTree}")

```
* Gradle <Interface>
* Settings <Interface>
* Task <Interface>
* Action <Interface>

## Gradle Life Cycle Phases
* Initialization

which project to execute, setting environment
map to one or more, *.gradle file
> First `init.gradle`
> `settings.gradle` to handle multi project. Which project to include in the build

Requires: build.gradle
* Configuration

Configure the project, requires `build.gradle`. Delegate object `Project`

* Execution

execution relies on build.gradle

![Gradle Life Cycle](gradle_phases.png)

## Use Case

![Gradle Life Cycle](cd.png)

`TDD approach`

We should stop at some point at early phase of dev,
to not continue with in implementation but write unit test

![TDD in action](tddinaction.png)

* Convert the requirement into unit test
```kotlin
class TestUpdateCustomer {
    
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
        
    }
}
```
* Write the failing test cases
* Make the test passed
