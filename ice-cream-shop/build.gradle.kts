plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    application
}

group = "com.icecream"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    
    // PostgreSQL JDBC Driver
    implementation("org.postgresql:postgresql:42.7.3")
    
    // Optional: Connection Pooling
    // implementation("com.zaxxer:HikariCP:5.1.0")
    
    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.9")
    
    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

application {
    mainClass.set("com.icecream.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
