import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "Fokin.Aleksandr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
    google()
}

dependencies {
    implementation("org.jetbrains.lets-plot:lets-plot-common:4.6.0")
    implementation("org.jetbrains.lets-plot:lets-plot-compose:2.2.1")
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.10.0")
    implementation("org.jetbrains.lets-plot:platf-awt:4.6.2")
    implementation("org.slf4j:slf4j-simple:2.0.12")
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            packageName = "NonlinearSolver"
            packageVersion = "1.0.0"
        }
    }
}
