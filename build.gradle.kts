import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.star_zero.gradle.githook.GithookExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "1.3.50"
    id("org.jlleitschuh.gradle.ktlint") version "7.1.0"
    id("com.star-zero.gradle.githook") version "1.1.0"
    id("com.github.johnrengelman.shadow") version "4.0.4"
}

fun version(): String {
    val buildNumber = System.getProperty("BUILD_NUM")
    val version = "0.1" + if (buildNumber.isNullOrEmpty()) "-SNAPSHOT" else ".$buildNumber"
    println("building version $version")
    return version
}

val projectVersion = version()

group = "io.klouds"
val artifactId = "db-migrator"
version = projectVersion

val http4kVersion = "3.183.0"

repositories {
    jcenter()
    mavenCentral()
    maven("https://dl.bintray.com/hexlabsio/kloudformation")
}

sourceSets {
    main {
        java {
            srcDirs("src/main/kotlin")
        }
    }
    test {
        java {
            srcDirs("src/test/kotlin", "stack")
        }
    }
}

val shadowJar by tasks.getting(ShadowJar::class) {
    archiveClassifier.set("uber")
    manifest {
        attributes(mapOf("Main-Class" to "io.klouds.migrator.RootHandlerKt"))
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(group = "org.jetbrains.exposed", name = "exposed", version = "0.17.3")
    compile("org.http4k:http4k-core:$http4kVersion")
    compile("org.http4k:http4k-format-jackson:$http4kVersion")
    compile("org.http4k:http4k-serverless-lambda:$http4kVersion")
    testImplementation(group = "org.jetbrains.kotlin", name = "kotlin-test-junit5", version = "1.3.21")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "1.3.21")
    testRuntime(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.0.0")
    testImplementation("io.kloudformation:kloudformation:1.1.19")
    testImplementation("io.hexlabs:kloudformation-serverless-module:1.1.1")
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

artifacts {
    add("archives", shadowJar)
}

configure<KtlintExtension> {
    outputToConsole.set(true)
    coloredOutput.set(true)
    reporters.set(setOf(ReporterType.CHECKSTYLE, ReporterType.JSON))
}

configure<GithookExtension> {
    githook {
        hooks {
            create("pre-commit") {
                task = "build"
            }
        }
    }
}