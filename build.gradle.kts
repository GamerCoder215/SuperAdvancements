@file:Suppress("UnstableApiUsage")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("org.sonarqube") version "4.0.0.2929"
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false

    java
    `maven-publish`
    `java-library`
    jacoco
}

val pGroup = "me.gamercoder215.superadvancements"
val pVersion = "1.0.1"
val pAuthor = "GamerCoder"

sonarqube {
    properties {
        property("sonar.projectKey", "${pAuthor}215_SuperAdvancements")
        property("sonar.organization", "gamercoder215")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

val jdProjects = listOf(
    ":superadvancements",
    ":superadvancements-spigot",
    ":superadvancements-paper"
)

tasks {
    register("allJavadoc", Javadoc::class.java) {
        jdProjects.forEach { dependsOn(project(it).tasks["javadoc"]) }

        enabled = true
        title = "SuperAdvancements $version API"

        source = files(jdProjects.map { project(it).sourceSets["main"].allJava }).asFileTree
        classpath = files(jdProjects.map { project(it).sourceSets["main"].compileClasspath })

        options {
            require(this is StandardJavadocDocletOptions)

            title = "SuperAdvancements ${project.version} API"
            encoding = "UTF-8"
            overview = "base/src/main/javadoc/overview.html"
            links("https://hub.spigotmc.org/javadocs/spigot/", "https://jd.advntr.dev/api/4.13.1/", "https://jd.papermc.io/paper/1.19/", "https://javadoc.io/doc/org.jetbrains/annotations/24.0.1/")
        }
    }
}

allprojects {
    group = pGroup
    version = pVersion
    description = "Advanced & Customizable Advancement API, made for SpigotMC 1.12+."

    apply(plugin = "maven-publish")
    apply<JavaPlugin>()
    apply<JavaLibraryPlugin>()

    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://jitpack.io")
        maven("https://plugins.gradle.org/m2/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://oss.sonatype.org/content/repositories/central")

        maven("https://repo.codemc.org/repository/nms/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://libraries.minecraft.net/")
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = pGroup
                artifactId = project.name
                version = pVersion

                pom {
                    description.set(project.description)

                    licenses {
                        license {
                            name.set("GPL-3.0")
                            url.set("https://github.com/GamerCoder215/SuperAdvancements/blob/master/LICENSE")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://GamerCoder215/SuperAdvancements.git")
                        developerConnection.set("scm:git:ssh://GamerCoder215/SuperAdvancements.git")
                        url.set("https://github.com/GamerCoder215/SuperAdvancements")
                    }

                    inceptionYear.set("2023")
                }

                from(components["java"])
            }

            repositories {
                maven {
                    credentials {
                        username = System.getenv("JENKINS_USERNAME")
                        password = System.getenv("JENKINS_PASSWORD")
                    }

                    val releases = "https://repo.codemc.io/repository/maven-releases/"
                    val snapshots = "https://repo.codemc.io/repository/maven-snapshots/"
                    url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshots else releases)
                }
            }
        }
    }
}

publishing {
    publications {
        getByName<MavenPublication>("maven") {
            pom {
                packaging = "pom"
            }
        }
    }
}

val jvmVersion = JavaVersion.VERSION_11

subprojects {
    apply<JacocoPlugin>()
    apply(plugin = "org.sonarqube")
    apply(plugin = "com.github.johnrengelman.shadow")

    dependencies {
        compileOnly("org.jetbrains:annotations:24.0.1")

        testImplementation("org.mockito:mockito-core:5.5.0")
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
        testImplementation("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    }

    java {
        sourceCompatibility = jvmVersion
        targetCompatibility = jvmVersion
    }

    publishing {
        publications {
            getByName<MavenPublication>("maven") {
                artifact(tasks["shadowJar"])
            }
        }
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
            options.isDeprecation = false
            options.isWarnings = false
            options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing"))
        }

        jacocoTestReport {
            dependsOn(test)

            reports {
                xml.required.set(false)
                csv.required.set(false)

                html.required.set(true)
                html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
            }
        }

        clean {
            delete("$projectDir/logs", buildDir)
        }

        test {
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")
            }

            classpath += sourceSets["main"].compileClasspath
            classpath += sourceSets["main"].allJava

            finalizedBy(jacocoTestReport)
        }

        javadoc {
            enabled = false
            options.encoding = "UTF-8"
            options.memberLevel = JavadocMemberLevel.PROTECTED
        }

        jar.configure {
            enabled = false
            dependsOn("shadowJar")
        }

        withType<ShadowJar> {
            manifest {
                attributes(
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to pAuthor
                )
            }

            exclude("META-INF", "META-INF/**")

            archiveClassifier.set("")
        }
    }
}