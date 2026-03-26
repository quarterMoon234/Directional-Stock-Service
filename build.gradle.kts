plugins {
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jetbrains.kotlinx.kover") version "0.9.7"
    id("com.google.devtools.ksp") version "2.3.6"
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.spring") version "2.3.20"
    kotlin("plugin.jpa") version "2.3.20"
}

group = "net.directional"
version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // spring boot
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // kotlin
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // database
    developmentOnly("org.springframework.boot:spring-boot-h2console")
    runtimeOnly("com.h2database:h2")

    // querydsl
    implementation("io.github.openfeign.querydsl:querydsl-jpa:7.1")
    ksp("io.github.openfeign.querydsl:querydsl-ksp-codegen:7.1")

    // swagger-ui
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")
    // test
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
