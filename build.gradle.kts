plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "ru.bash"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("ru.bash.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
