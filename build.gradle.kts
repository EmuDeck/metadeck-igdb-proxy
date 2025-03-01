val ktor_version: String by project
val okio_version: String by project
val exposed_version: String by project
val h2_version: String by project
val logback_version: String by project

plugins {
    kotlin("multiplatform") version "1.9.0"
	kotlin("plugin.serialization") version "1.9.0"
	id("com.github.johnrengelman.shadow") version "7.1.1"
	application
}

group = "com.emudeck"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    jvm {
        jvmToolchain(19)
        withJava()
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
    js {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
	            implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
	            implementation("io.ktor:ktor-server-netty:$ktor_version")
                implementation("io.ktor:ktor-server-html-builder-jvm:$ktor_version")
	            implementation("io.ktor:ktor-server-call-logging:$ktor_version")
	            implementation("io.ktor:ktor-server-cors:$ktor_version")
	            implementation("io.ktor:ktor-server-swagger:$ktor_version")

	            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

	            implementation("io.ktor:ktor-client-core:$ktor_version")
	            implementation("io.ktor:ktor-client-apache5:$ktor_version")
	            implementation("io.ktor:ktor-client-auth:$ktor_version")
	            implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")

	            implementation("ch.qos.logback:logback-classic:$logback_version")

	            implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")

	            implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
	            implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
	            implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
	            implementation("com.h2database:h2:$h2_version")

	            implementation("com.squareup.okio:okio:$okio_version")

	            implementation("io.github.reactivecircus.cache4k:cache4k:0.11.0")
            }
        }
        val jvmTest by getting {
			dependencies {
				implementation(kotlin("test"))
			}
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.9.3-pre.346")
            }
        }
        val jsTest by getting
    }
}

application {
    mainClass.set("com.emudeck.igdb_proxy.ServerKt")
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}

tasks {
	"build" {
		dependsOn(shadowJar)
	}
}
