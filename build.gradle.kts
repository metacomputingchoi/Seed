plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.json:json:20230227")
    implementation(project(":MCalendar"))
}