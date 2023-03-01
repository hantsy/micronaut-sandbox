plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    id("org.jetbrains.kotlin.kapt") version "1.8.10"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.8.10"
    id("com.github.johnrengelman.shadow") version "8.1.0"
    id("io.micronaut.application") version "3.7.3"
    id("io.micronaut.test-resources") version "3.7.4"
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
java {
    sourceCompatibility = JavaVersion.toVersion("17")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    test {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
        }
    }
}

graalvmNative.toolchainDetection.set(false)
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
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // micronaut framework
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("io.micronaut.reactor:micronaut-reactor-http-client")

    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-validation")

    //database
    implementation("io.micronaut.data:micronaut-data-jdbc:3.9.6")
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
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}
