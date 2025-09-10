plugins {
    id("org.jetbrains.intellij.platform") version "2.9.0"
    id("org.jetbrains.kotlin.jvm") version "2.2.20"
    id("idea")
}

group = "dev.kilua"
version = "0.0.3"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:adapter-rxjava3:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("io.reactivex.rxjava3:rxjava:3.1.11")
    implementation("io.reactivex.rxjava3:rxkotlin:3.0.1")
    intellijPlatform {
        intellijIdeaCommunity("2025.2.1")
        bundledPlugin("com.intellij.java")
        bundledPlugin("com.intellij.gradle")
        bundledPlugin("org.jetbrains.kotlin")
        pluginVerifier()
    }
}

kotlin {
    jvmToolchain(21)
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellijPlatform {
    buildSearchableOptions = false
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
            untilBuild = provider { null }
        }
    }
}
