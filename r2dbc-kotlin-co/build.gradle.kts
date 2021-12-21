plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    id("org.jetbrains.kotlin.kapt") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.1"
    id("io.micronaut.application") version "3.1.1"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.6.10"
}

version = "0.1"
group = "com.example"

val kotlinVersion = project.properties.get("kotlinVersion")
val kotlinCoVersion = project.properties.get("kotlinCoVersion")

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

    // javaee/jakarta ee spec
    implementation("jakarta.annotation:jakarta.annotation-api:2.0.0")
    implementation("jakarta.persistence:jakarta.persistence-api:3.0.0")

    //micronaut framework
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut.data:micronaut-data-r2dbc")
    runtimeOnly("io.r2dbc:r2dbc-postgresql")

    //kotlin support
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    //logging
    runtimeOnly("ch.qos.logback:logback-classic")

    //reactor/reactivestreams httpclient
    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("io.micronaut.reactor:micronaut-reactor-http-client")

    //kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${kotlinCoVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${kotlinCoVersion}")

    // annotation processor
    kapt("io.micronaut:micronaut-http-validation")
    kapt("io.micronaut.data:micronaut-data-processor")

    // test
    // https://mvnrepository.com/artifact/io.projectreactor/reactor-test
    testImplementation("io.projectreactor:reactor-test:3.4.13")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${kotlinCoVersion}")
    //testImplementation("org.testcontainers:r2dbc")
    //testImplementation("org.testcontainers:testcontainers")
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
            freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
    }
}
