import java.io.FileInputStream
import java.util.Properties

plugins {
    `kotlin-dsl`
    id("signing")
    id("com.gradle.plugin-publish") version "1.3.0"
}

sourceSets {
    main {
        kotlin.srcDirs("src")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

group = "io.github.tomictomek"
version = "1.0.0"

val githubProperties = Properties().apply {
    val propertiesFile = rootProject.file("github.properties")
    if (propertiesFile.exists()) {
        load(FileInputStream(propertiesFile))
    }
}

gradlePlugin {
    website = "https://github.com/TomicTomek/version-prop-plugin"
    vcsUrl = "https://github.com/TomicTomek/version-prop-plugin.git"

    plugins {
        create("versionFromPropertyFile") {
            id = "io.github.tomictomek.version-prop"
            tags = listOf("version", "properties")
            implementationClass = "pl.tomasz.jokiel.VersionPropPlugin"
            displayName = "Version from property file"
            description = "The util to easily define the app version in the property file."
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/TomicTomek/version-prop-plugin")
            credentials {
                /** Create github.properties in root project folder file with
                 * gpr.usr=GITHUB_USER_ID & gpr.key=PERSONAL_ACCESS_TOKEN
                 * Set env variable GPR_USER & GPR_API_KEY if not adding a properties file
                 * Personal Access Token (classic) has to have packages:write permissions
                 * **/

                username = "${githubProperties["gpr.usr"] ?: System.getenv("GPR_USER")}"
                password = "${githubProperties["gpr.key"] ?: System.getenv("GPR_API_KEY")}"
            }
        }

        /**
         * ./gradlew ./gradlew publishAllPublicationsToLocalRepository
         * To publish to local repository. Results stored in <PROJECT-ROOT>/build/local-repo
         * To create manually deployable ZIP package, enter <PROJECT-ROOT>/build/local-repo
         * Run
         * zip  -rq  ../name.zip .
         */
        maven {
            name = "Local"
            url = uri(layout.buildDirectory.dir("local-repo"))
        }
    }
}

afterEvaluate {
    tasks.withType(GenerateMavenPom::class.java) {
        doFirst {
            println("doFirst")
            with(pom) {
                name = "Version from property file"
                description = "The util to easily define the app version in the property file."
                url = "https://github.com/TomicTomek/version-prop-plugin/"
                scm {
                    connection = "scm:git:https://github.com/TomicTomek/version-prop-plugin/"
                    developerConnection = "scm:git:https://github.com/TomicTomek/version-prop-plugin/"
                    url = "https://github.com/TomicTomek/version-prop-plugin/"
                }
                licenses {
                    license {
                        name = "GPL-3.0 license"
                        url = "https://www.gnu.org/licenses/gpl-3.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "tjokiel"
                        name = "Tomasz Jokiel"
                        email = "tomasz.jokiel@gmail.com"
                    }
                }
            }
        }
    }
}