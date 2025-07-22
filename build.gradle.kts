import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("jvm") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `maven-publish`
}

group = "com.github.vanes430.autotool"
version = "1.0"
description = "AutoTool"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenLocal()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    mavenCentral()
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    implementation(kotlin("stdlib-jdk8"))
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

val pluginVersion: String by extra(version.toString())

tasks.processResources {
    val versionProvider = providers.gradleProperty("version").orElse(pluginVersion)
    filesMatching("plugin.yml") {
        expand(mapOf("version" to versionProvider.get()))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveVersion.set(pluginVersion)
        archiveClassifier.set("")
        minimize()
    }
}

tasks.named<Jar>("jar") {
    enabled = false
}
