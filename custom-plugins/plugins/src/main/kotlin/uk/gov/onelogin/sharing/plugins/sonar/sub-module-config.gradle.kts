package uk.gov.onelogin.sharing.plugins.sonar

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.sonarqube.gradle.SonarExtension
import uk.gov.onelogin.sharing.plugins.Filters
import uk.gov.onelogin.sharing.plugins.PluginManagerExtensions.isAndroidApp
import uk.gov.onelogin.sharing.plugins.PluginManagerExtensions.isAndroidLibrary
import uk.gov.onelogin.sharing.plugins.PluginManagerExtensions.isJavaLibrary

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

listOf(
    "sonarqube"
).map { versionCatalogId ->
    libs.findPlugin(versionCatalogId).get().get().pluginId
}.forEach(pluginManager::apply)

val testSourceRegex = Regex(
    "${layout.projectDirectory.asFile.absolutePath}/src/.*?[tT]est.*?/.*"
)

val androidLintReportFiles = fileTree(
    layout.buildDirectory.dir("reports")
).matching {
    include(
        "lint-results-*.xml",
    )
}.joinToString(",") {
    it.absolutePath
}

val detektReportFiles = fileTree(
    layout.buildDirectory.dir("reports/detekt")
).matching {
    include("*.xml")
}.joinToString(",") {
    it.absolutePath
}

val jacocoXmlReportFiles = (fileTree(
    layout.buildDirectory.dir("reports/jacoco")
).matching {
    include("**/*.xml")
} + fileTree(
    layout.buildDirectory.dir("reports/coverage")
).matching {
    include("**/*.xml")
}).joinToString(",") {
    it.absolutePath
}

val junitReportFiles by project.extra(
    generateCommaSeparatedFiles(
        listOf(
            // instrumentation
            "**/outputs/androidTest-results/managedDevice/*",
            // unit tests
            "**/test-results",
        ),
    ),
)

val ktLintReportFiles = fileTree(
    layout.buildDirectory.dir("reports/ktlint")
).matching {
    include("**/*.xml")
}.joinToString(",") {
    it.absolutePath
}

val sonarExclusions by project.extra(
    listOf(
        Filters.androidInstrumentationTests,
        Filters.sonar,
        Filters.testSourceSets,
        Filters.developer,
    ).flatten().joinToString(separator = ","),
)

if (pluginManager.isAndroidApp()) {
    configure<ApplicationAndroidComponentsExtension> {
        onVariants { variant ->
            val (productionSources, testSources) = fetchKotlinSources(
                variant.sources.kotlin?.all
            )

            configureSonarExtension(
                generateBaseSonarProperties(
                    productionSources = productionSources,
                    testSources = testSources,
                ) + mapOf(
                    "sonar.androidLint.reportPaths" to androidLintReportFiles,
                )
            )
        }
    }
} else if (pluginManager.isAndroidLibrary()) {
    configure<LibraryAndroidComponentsExtension> {
        onVariants { variant ->
            val (productionSources, testSources) = fetchKotlinSources(
                variant.sources.kotlin?.all
            )

            configureSonarExtension(
                generateBaseSonarProperties(
                    productionSources = productionSources,
                    testSources = testSources,
                ) + mapOf(
                    "sonar.androidLint.reportPaths" to androidLintReportFiles,
                )
            )
        }
    }
} else if (pluginManager.isJavaLibrary()) {
    val conversion: (KotlinSourceSet) -> String = {
        it.kotlin.filter(File::exists)
            .joinToString(",") { file ->
            file.absolutePath
        }
    }
    configure<KotlinJvmProjectExtension> {
        val productionSources = sourceSets.named("main").map(conversion)
        val testSources = sourceSets.named("test").map(conversion)

        configureSonarExtension(
            generateBaseSonarProperties(
                productionSources = productionSources,
                testSources = testSources,
            )
        )
    }
}

fun generateCommaSeparatedFiles(
    iterator: Iterable<String>
) = fileTree(layout.projectDirectory) {
    this.setIncludes(iterator)
}.files.joinToString(
    separator = ",",
    transform = File::getAbsolutePath,
)

fun generateBaseSonarProperties(
    productionSources: Provider<String>?,
    testSources: Provider<String>?,
) = mapOf<String, Any?>(
    "sonar.sources" to productionSources?.get(),
    "sonar.tests" to testSources?.get(),
    "sonar.exclusions" to sonarExclusions,
    "sonar.coverage.jacoco.xmlReportPaths" to jacocoXmlReportFiles,
    "sonar.kotlin.detekt.reportPaths" to detektReportFiles,
    "sonar.kotlin.ktlint.reportPaths" to ktLintReportFiles,
    "sonar.junit.reportPaths" to junitReportFiles,
)

fun configureSonarExtension(
    sonarProperties: Map<String, Any?>
) {
    configure<SonarExtension> {
        properties {
            sonarProperties.forEach { (key: String, value: Any?) ->
                value?.let {
                    property(key, it)
                    project.logger.debug("SONAR {} {}", key, it)
                }
            }
        }
    }
}

fun fetchKotlinSources(
    kotlinSources: Provider<out Collection<Directory>>?,
): Pair<Provider<String>?, Provider<String>?> = kotlinSources?.map { directories ->
    directories.filter { directory ->
        !directory.asFile.absolutePath.matches(testSourceRegex)
    }.filter { it.asFile.exists() }
}?.map(
    ::asCommaSeparatedString
) to kotlinSources?.map { directories ->
    directories.filter { directory ->
        directory.asFile.absolutePath.matches(testSourceRegex)
    }.filter { it.asFile.exists() }
}?.map(::asCommaSeparatedString)

fun asCommaSeparatedString(
    directories: List<Directory>
) = directories.joinToString(",") { directory ->
    directory.asFile.absolutePath
}