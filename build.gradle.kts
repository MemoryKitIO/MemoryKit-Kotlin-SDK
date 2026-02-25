plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.serialization") version "1.9.25"
    `maven-publish`
    signing
}

group = property("GROUP") as String
version = property("VERSION_NAME") as String

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    // HTTP
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}

kotlin {
    jvmToolchain(8)
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            groupId = property("GROUP") as String
            artifactId = property("POM_ARTIFACT_ID") as String
            version = property("VERSION_NAME") as String

            pom {
                name.set(property("POM_NAME") as String)
                description.set(property("POM_DESCRIPTION") as String)
                url.set(property("POM_URL") as String)

                licenses {
                    license {
                        name.set(property("POM_LICENCE_NAME") as String)
                        url.set(property("POM_LICENCE_URL") as String)
                    }
                }

                developers {
                    developer {
                        id.set(property("POM_DEVELOPER_ID") as String)
                        name.set(property("POM_DEVELOPER_NAME") as String)
                        url.set(property("POM_DEVELOPER_URL") as String)
                    }
                }

                scm {
                    url.set(property("POM_SCM_URL") as String)
                    connection.set(property("POM_SCM_CONNECTION") as String)
                    developerConnection.set(property("POM_SCM_DEV_CONNECTION") as String)
                }
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

            credentials {
                username = findProperty("ossrhUsername") as? String ?: System.getenv("OSSRH_USERNAME") ?: ""
                password = findProperty("ossrhPassword") as? String ?: System.getenv("OSSRH_PASSWORD") ?: ""
            }
        }
    }
}

signing {
    val signingKey = findProperty("signingKey") as? String ?: System.getenv("SIGNING_KEY")
    val signingPassword = findProperty("signingPassword") as? String ?: System.getenv("SIGNING_PASSWORD")
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["maven"])
    }
}
