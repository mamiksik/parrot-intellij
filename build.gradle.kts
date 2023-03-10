plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.11.0"
    id("co.uzzu.dotenv.gradle") version "2.0.0"
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
}

dependencies {
    implementation(kotlin("stdlib-common"))
    implementation(group = "com.github.kittinunf.fuel", name = "fuel", version = "main-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

group = "com.mamiksik"
version = "1.2.8-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.jetbrains.team/maven/p/ki/maven")
    }

    maven(url = "https://www.jitpack.io") {
        name = "jitpack"
    }
}


// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2")
    type.set("PY") // Target IDE Platform

    plugins.set(listOf("git4idea"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("223.*")
    }

    signPlugin {
        certificateChain.set(env.CERTIFICATE_CHAIN.value)
        privateKey.set(env.PRIVATE_KEY.value)
        password.set(env.PRIVATE_KEY_PASSWORD.value)
    }

    publishPlugin {
//        channels.set(listOf("beta"))
        token.set(env.JETBRAINS_API_TOKEN.value)
    }

    runIde {
        jvmArgs = listOf("-Xmx2G")
    }
}
