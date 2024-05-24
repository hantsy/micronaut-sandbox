plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.0.0"
    id("com.google.devtools.ksp") version "2.0.0-1.0.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.4.0"
    id("io.micronaut.test-resources") version "4.4.0"
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
    implementation("io.projectreactor:reactor-core:3.6.6")

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
    testImplementation("io.projectreactor:reactor-test:3.6.6")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${kotlinCoVersion}")
    testResourcesService("org.postgresql:postgresql")
}

application {
    mainClass.set("com.example.ApplicationKt")
}

java {
    sourceCompatibility = JavaVersion.toVersion("21")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "21"
            freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "21"
            freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        }
    }
    test{
        useJUnitPlatform()
    }
}
