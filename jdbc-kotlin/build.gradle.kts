plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.21"
    id("org.jetbrains.kotlin.kapt") version "1.5.21"
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("io.micronaut.application") version "2.0.6"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.5.21"
}

version = "0.1"
group = "com.example"

val kotlinVersion= project.properties["kotlinVersion"]
repositories {
    mavenCentral()
}

micronaut {
    runtime("netty")
    testRuntime("kotest")
    processing {
        incremental(true)
        annotations("com.example.*")
    }
}

dependencies {
    // javaee/jakartaee specs
    implementation("javax.annotation:javax.annotation-api")

    // micronaut framework
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-validation")

    //database
    implementation("io.micronaut.data:micronaut-data-jdbc")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    runtimeOnly("org.postgresql:postgresql")

    //kotlin
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    //logging
    runtimeOnly("ch.qos.logback:logback-classic")

    // kapt
    kapt("io.micronaut:micronaut-http-validation")
    kapt("io.micronaut.data:micronaut-data-processor")

    //test
    //testImplementation("org.testcontainers:postgresql:1.16.0")
    //testImplementation("org.testcontainers:testcontainers:1.16.0")
    testImplementation("io.mockk:mockk")
    testApi("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("io.kotest:kotest-runner-junit5-jvm")
    testImplementation("io.kotest:kotest-assertions-core-jvm")
}


application {
    mainClass.set("com.example.ApplicationKt")
}
java {
    sourceCompatibility = JavaVersion.toVersion("11")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}
