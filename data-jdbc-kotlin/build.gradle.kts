import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.2.21"
    id("com.google.devtools.ksp") version "2.3.2"
    id("com.gradleup.shadow") version "9.2.2"
    id("io.micronaut.application") version "4.6.1"
    id("io.micronaut.test-resources") version "4.6.1"
}

version = "0.1"
group = "com.example"

val kotlinVersion = project.properties.get("kotlinVersion")

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.example.ApplicationKt")
}

kotlin {
    // see: https://blog.allegro.tech/2024/11/popular-gradle-mistakes-and-how-to-avoid-them.html
    jvmToolchain(21)
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
        jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            // https://slack-chats.kotlinlang.org/t/27630676/with-2-2-0-beta2-bump-version-i-am-getting-identity-sensitiv
            "-Xwarning-level=IDENTITY_SENSITIVE_OPERATIONS_WITH_VALUE_TYPE:disabled"
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
}

graalvmNative.toolchainDetection.set(false)

micronaut {
    runtime("netty")
    testRuntime("kotest5")
    processing {
        incremental(true)
        annotations("com.example.*")
    }
}

dependencies {
    // javaee/jakartaee specs
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // micronaut framework
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("io.micronaut.reactor:micronaut-reactor-http-client")

    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.micronaut.validation:micronaut-validation")

    //database
    implementation("io.micronaut.data:micronaut-data-jdbc")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    runtimeOnly("org.postgresql:postgresql")

    //flyway
    implementation("io.micronaut.flyway:micronaut-flyway")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")

    //kotlin
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    // yaml support
    runtimeOnly("org.yaml:snakeyaml")

    //logging
    runtimeOnly("ch.qos.logback:logback-classic")

    // Google KSP
    ksp("io.micronaut.validation:micronaut-validation-processor")
    ksp("io.micronaut.data:micronaut-data-processor")
}
