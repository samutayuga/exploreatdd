import org.jetbrains.kotlin.daemon.nowSeconds
val kotlinVersion: String by project
val jupyterVersion: String by project
val kotlinMockVersion: String by project
val exposedVersion: String by project
val postgresqlVersion: String by project
plugins {
    kotlin("jvm") version "1.6.21"
    application
    id("org.jetbrains.kotlinx.kover") version "0.4.2"
}
val test:String = """
    Welcome to project ${rootProject.name}
""".trimIndent()
logger.info("This is the starting of the build script")
apply {
    println("$test")

}
//println("This is in ${file(".")}")
//println("File tree ${fileTree(".")}")
//println("File tree ${fileTree("src").asFileTree}")
logger.info("what is build dir> ${project.buildDir}")
//logger.info("what is build file> ${project.buildFile}")
//attach string into project delegate object

//val testing: (String) -> Unit = { logger.info("this is current ${nowSeconds()} time in the project $it")}

//testing("${project.buildDir}")

//logger.info("what is kotlin version> $kotlinVersion")

group = "me.putumas"
version = "0.1.0-SNAPSHOT"

logger.info("time now is ${nowSeconds()}")
repositories {
    mavenCentral()
}
dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupyterVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupyterVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${jupyterVersion}")
    testImplementation("io.mockk:mockk:${kotlinMockVersion}")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("com.zaxxer:HikariCP:3.3.1")
    implementation("com.impossibl.pgjdbc-ng", "pgjdbc-ng", "0.8.3")

    //testImplementation("org.postgresql:postgresql:$postgresqlVersion")
    testImplementation("org.postgresql:postgresql:42.3.3")
}
tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}
tasks.jar {
    dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(mapOf("Main-Class" to application.mainClass))
    }
    val sourceMain = sourceSets.main.get()
    val contents =
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) } + sourceMain.output
    from(contents)
}
