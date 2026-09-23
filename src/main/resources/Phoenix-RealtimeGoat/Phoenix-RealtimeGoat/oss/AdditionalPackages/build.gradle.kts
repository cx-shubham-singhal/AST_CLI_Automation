package `Phoenix-RealtimeGoat`.`Phoenix-RealtimeGoat`.oss.`selected-manifest-files`

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
    id("org.springframework.boot") version "2.5.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

// TC_06: Kotlin val / const val — parser must resolve these
val jacksonVersion      = "2.12.3"           // CVE-2020-36518
val log4jVersion        = "2.14.1"           // CVE-2021-44228 Log4Shell
val nettyVersion        = "4.1.59.Final"     // CVE-2021-21290
const val lombokVersion = "1.18.16"
val snakeYamlVersion    = "1.29"             // CVE-2022-1471
val springSecVersion    = "5.3.13"           // CVE-2022-22978
val xstreamVersion      = "1.4.15"           // CVE-2021-29505
val commonsTextVersion  = "1.9"              // CVE-2022-42889
val h2Version           = "1.4.200"          // CVE-2021-42392
val guavaVersion        = "29.0-jre"         // CVE-2020-8908
val fastjsonVersion     = "1.2.62"           // CVE-2019-17558
val hibernateVersion    = "5.4.32.Final"     // CVE-2019-14900

group = "com.example"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // TC_06: val variables resolved by parser
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("io.netty:netty-all:$nettyVersion")
    implementation("org.yaml:snakeyaml:$snakeYamlVersion")
    implementation("org.springframework.security:spring-security-core:$springSecVersion")
    implementation("com.thoughtworks.xstream:xstream:$xstreamVersion")
    implementation("org.apache.commons:commons-text:$commonsTextVersion")
    implementation("com.h2database:h2:$h2Version")
    implementation("com.google.guava:guava:$guavaVersion")
    implementation("com.alibaba:fastjson:$fastjsonVersion")
    implementation("org.hibernate:hibernate-core:$hibernateVersion")

    // Additional hardcoded vulnerable deps
    implementation("org.projectlombok:lombok:$lombokVersion")
    implementation("commons-collections:commons-collections:3.2.1")   // CVE-2015-6420
    implementation("org.apache.commons:commons-compress:1.20")         // CVE-2021-35516
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("io.jsonwebtoken:jjwt:0.9.1")                       // CVE-2022-21449
    implementation("org.bouncycastle:bcprov-jdk15on:1.64")             // CVE-2020-15522
    implementation("org.apache.struts:struts2-core:2.5.26")            // CVE-2021-31805
    implementation("net.minidev:json-smart:2.3")                        // CVE-2021-31684
    implementation("org.apache.velocity:velocity:1.7")                 // CVE-2020-13936

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("junit:junit:4.13.1")                           // CVE-2020-15250
    testImplementation("org.mockito:mockito-kotlin:4.0.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
