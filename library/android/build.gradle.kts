plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("maven-publish")
    id("signing")
    id("com.gradleup.nmcp")
}

group = "one.adverse"
version = providers.gradleProperty("VERSION_NAME").get()

android {
    namespace = "one.adverse.progress"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    api("androidx.compose.ui:ui:1.10.4")
    implementation("androidx.compose.foundation:foundation:1.10.4")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16")
    testImplementation("androidx.compose.ui:ui-test-junit4:1.10.4")
    testImplementation("androidx.compose.ui:ui-test-manifest:1.10.4")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "one.adverse"
            artifactId = "perimeter-progress"
            version = project.version.toString()

            pom {
                name.set("Adverse Progress")
                description.set("Reusable perimeter and linear progress components for Android.")
                url.set("https://github.com/snooplsm/perimeter-progress")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/license/mit")
                    }
                }

                developers {
                    developer {
                        id.set("snooplsm")
                        name.set("Adverse")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/snooplsm/perimeter-progress.git")
                    developerConnection.set("scm:git:ssh://github.com/snooplsm/perimeter-progress.git")
                    url.set("https://github.com/snooplsm/perimeter-progress")
                }
            }
        }
    }

    repositories {
        maven {
            name = "localStaging"
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

afterEvaluate {
    publishing {
        publications.named<MavenPublication>("release") {
            from(components["release"])
        }
    }
}

val signingKey = providers.gradleProperty("signingInMemoryKey")
    .orElse(providers.environmentVariable("SIGNING_KEY"))
    .orNull
val signingPassword = providers.gradleProperty("signingInMemoryKeyPassword")
    .orElse(providers.environmentVariable("SIGNING_PASSWORD"))
    .orNull

signing {
    if (!signingKey.isNullOrBlank()) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}
