plugins {
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))
    implementation("org.springframework:spring-context")

    testImplementation("io.mockk:mockk:1.13.13")
}
