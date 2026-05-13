import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android") version "2.3.21"
}

group = "com.deepanshuchaudhary.pdf_manipulator"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

android {
    namespace = "com.deepanshuchaudhary.pdf_manipulator"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
        }

        getByName("test") {
            java.srcDirs("src/test/kotlin")
        }
    }
}

dependencies {
    implementation("com.itextpdf:kernel:9.6.0")
    implementation("com.itextpdf:layout:9.6.0")
    implementation("com.itextpdf:io:9.6.0")
    implementation("com.itextpdf:itextpdf:5.5.13.5")
    implementation("org.bouncycastle:bcprov-jdk18on:1.84")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.84")
    implementation("com.github.bumptech.glide:gifencoder-integration:5.0.7")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("com.google.mlkit:text-recognition:16.0.1")

    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:5.23.0")
}
