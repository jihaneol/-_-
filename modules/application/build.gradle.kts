plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":domain"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("io.swagger.core.v3:swagger-annotations-jakarta:2.2.49")
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")
    implementation("org.springframework.data:spring-data-commons")

    testImplementation("io.mockk:mockk:1.13.13")
}
