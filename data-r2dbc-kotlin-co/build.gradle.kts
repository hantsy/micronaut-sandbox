import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.10"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.2.0"
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("com.gradleup.shadow") version "8.3.8"
    id("io.micronaut.application") version "4.5.4"
    id("io.micronaut.test-resources") version "4.5.4"
}

version = "0.1"
group = "com.example"

val kotlinVersion = project.properties.get("kotlinVersion")
val kotlinCoVersion = project.properties.get("kotlinCoVersion")
val kotestVersion = project.properties.get("kotestVersion")

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

allOpen {
    annotation("io.micronaut.http.annotation.Controller")
    // annotations("com.another.Annotation", "com.third.Annotation")
}

micronaut {
    runtime("netty")
    testRuntime("kotest5")
    processing {
        incremental(true)
        annotations("com.example.*")
    }
}

dependencies {

    // javaee/jakarta ee spec
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    //micronaut framework
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("io.micronaut.reactor:micronaut-reactor-http-client")
    implementation("io.micronaut.validation:micronaut-validation")
    implementation("io.micronaut.data:micronaut-data-r2dbc")
    runtimeOnly("org.postgresql:r2dbc-postgresql")
    implementation("io.projectreactor:reactor-core:3.7.8")

    //kotlin support
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    //flyway
    implementation("io.micronaut.flyway:micronaut-flyway")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    runtimeOnly("org.postgresql:postgresql")

    // yaml support
    runtimeOnly("org.yaml:snakeyaml")

    //logging
    runtimeOnly("ch.qos.logback:logback-classic")

    //kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${kotlinCoVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${kotlinCoVersion}")

    // ksp
    ksp("io.micronaut.data:micronaut-data-processor")
    ksp("io.micronaut.validation:micronaut-validation-processor")

    // test
    // https://mvnrepository.com/artifact/io.projectreactor/reactor-test
    testImplementation("io.projectreactor:reactor-test:3.7.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${kotlinCoVersion}")
    testResourcesService("org.postgresql:postgresql")
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