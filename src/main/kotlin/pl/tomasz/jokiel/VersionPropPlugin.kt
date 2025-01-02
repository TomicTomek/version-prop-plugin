/**
 * In order to import that plugin:
 * <PROJECT_ROOT>/settings.gradle.kts
 * pluginManagement {
 *   repositories {
 *      mavenLocal()
 *   }
 * }
 *
 * <PROJECT_ROOT>/app/build.gradle.kts
 * plugins {
 *  id("jsp.version-prop") version "1.0.0"
 * }
 *
 * Usage:
 * <PROJECT_ROOT>/app/build.gradle.kts
 *
 * versionPropDef {
 *     propertyFile = rootProject.layout.projectDirectory.file("version.properties")
 *     versionNameSuffix = System.getenv("BUILD_RUN_NUMBER")
 * }
 * ...
 * android {
 *  defaultConfig {
 *    ...
 *    versionCode = versionPropDef.version.code
 *    versionName = versionPropDef.version.name
 *  }
 * }
 */
package pl.tomasz.jokiel

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*


data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val code: Int,
    val name: String
)


interface VersionPropExtension {
    /**
     *
     * propertyFile = rootProject.layout.projectDirectory.file("version.properties")
     */
    val propertyFile: RegularFileProperty
    val versionNameSuffix: Property<String?>

    val version: Version
        get() {
            val versionProperties = Properties().apply {
                propertyFile.get().asFile.takeIf { it.exists() }?.let {
                    load(FileInputStream(it))
                } ?: run {
                    throw FileNotFoundException("${propertyFile.get().asFile.absolutePath} doesn't exist")
                }
            }
            val major = versionProperties["version.major"].toString().toInt()
            val minor = versionProperties["version.minor"].toString().toInt()
            val patch = versionProperties["version.patch"].toString().toInt()
            val code = versionProperties["version.code"].toString().toInt()
            return Version(
                major = major,
                minor = minor,
                patch = patch,
                code = code,
                name = buildVersionName(major, minor, patch, versionNameSuffix.orNull),
            )
        }

    private fun buildVersionName(major: Int, minor: Int, patch: Int, suffix: String?): String {
        val versionBase = "$major.$minor.$patch"

        return if (!suffix.isNullOrBlank()) {
            "$versionBase.$suffix"
        } else versionBase
    }

}

class VersionPropPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // define and configure extension
        val extensionVariant = project.extensions.create<VersionPropExtension>("versionPropDef")
        extensionVariant.propertyFile.convention(project.rootProject.layout.projectDirectory.file("version.properties"))

        project.task("printVersionFileContent") {
            doLast {
                val propertyFile = extensionVariant.propertyFile.get().asFile
                if (!propertyFile.exists()) {
                    throw FileNotFoundException("$propertyFile doesn't exist")
                }
                val fileContent = propertyFile.readText()
                println("[VersionPropPlugin.task] Properties file, located at ${propertyFile.absolutePath} nas content:\n$fileContent")
            }
        }
    }
}