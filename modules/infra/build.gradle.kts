plugins {
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    kotlin("kapt")
}

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.security:spring-security-crypto")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")

    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")

    runtimeOnly("com.mysql:mysql-connector-j")
    testImplementation("io.mockk:mockk:1.13.13")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
