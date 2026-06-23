plugins {
    kotlin("plugin.jpa")
    kotlin("kapt")
}

dependencies {
    implementation("jakarta.persistence:jakarta.persistence-api")

    compileOnly("com.querydsl:querydsl-core:5.1.0")
    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")
}
