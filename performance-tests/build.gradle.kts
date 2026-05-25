plugins {
    java
    id("io.gatling.gradle") version "3.15.0.3"
}

repositories {
    mavenCentral()
}

gatling {
    jvmArgs = listOf(
        "-server",
        "-Xms512m",
        "-Xmx1g",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED"
    )
    systemProperties = mapOf("baseUrl" to "http://localhost:8080")
}
