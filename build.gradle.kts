plugins {
    id("java")
    id("signing")
    id("maven-publish")
    id("com.diffplug.spotless") version "6.24.0"
    id("com.gradleup.nmcp") version "0.0.4"
}

group = "io.github.overcat"
version = "1.0.0"

spotless {
    java {
        importOrder("java", "javax", "io.github")
        removeUnusedImports()
        googleJavaFormat()
    }
}

repositories {
    mavenCentral()
}

tasks {
    test {
        useJUnitPlatform()
    }

    val sourcesJar by creating(Jar::class) {
        archiveClassifier = "sources"
        from(sourceSets.main.get().allSource)
    }

    val uberJar by creating(Jar::class) {
        archiveClassifier = "uber"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(sourceSets.main.get().output)
        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }

    javadoc {
        setDestinationDir(file("javadoc"))
        isFailOnError = false
        options {
            // https://docs.gradle.org/current/javadoc/org/gradle/external/javadoc/StandardJavadocDocletOptions.html
            this as StandardJavadocDocletOptions
            isSplitIndex = true
            memberLevel = JavadocMemberLevel.PUBLIC
            encoding = "UTF-8"
        }
    }

    val javadocJar by creating(Jar::class) {
        archiveClassifier = "javadoc"
        dependsOn(javadoc)
        from(javadoc.get().destinationDir) // It needs to be placed after the javadoc task, otherwise it cannot read the path we set.
    }
}

artifacts {
    archives(tasks.jar)
    archives(tasks["uberJar"])
    archives(tasks["sourcesJar"])
    archives(tasks["javadocJar"])
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["uberJar"])
            artifact(tasks["javadocJar"])
            artifact(tasks["sourcesJar"])
            pom {
                name.set("boilerplate")
                description.set("A description of what my library does.")
                url.set("https://github.com/overcat/boilerplate-java-library")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("overcat")
                        name.set("Jun Luo")
                        url.set("https://github.com/overcat/")
                    }
                    organization {
                        name.set("overcat")
                        url.set("https://github.com/overcat")
                    }
                }
                scm {
                    url.set("https://github.com/overcat/boilerplate-java-library")
                    connection.set("scm:git:git://github.com/overcat/boilerplate-java-library.git")
                    developerConnection.set("scm:git:ssh://git@github.com/overcat/boilerplate-java-library.git")
                }
            }
        }
    }
}

signing {
    // https://docs.gradle.org/current/userguide/signing_plugin.html#using_in_memory_ascii_armored_openpgp_subkeys
    // export ORG_GRADLE_PROJECT_signingKey=$(gpg2 --export-secret-keys --armor {keyId} | grep -v '\-\-' | grep -v '^=.' | tr -d '\n')
    val signingKey = System.getenv("SIGNING_KEY")
    val signingKeyId = System.getenv("SIGNING_KEY_ID")
    val signingPassword = System.getenv("SIGNING_PASSWORD")
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}

nmcp {
    // https://github.com/GradleUp/nmcp
    publishAllProjectsProbablyBreakingProjectIsolation {
        username = System.getenv("SONATYPE_USERNAME")
        password = System.getenv("SONATYPE_PASSWORD")
        // publish manually from the portal
        publicationType = "USER_MANAGED"
        // or if you want to publish automatically
        // publicationType = "AUTOMATIC"
    }
}