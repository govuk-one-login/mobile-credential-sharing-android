package uk.gov.onelogin.sharing.plugins.testing

import com.android.build.api.artifact.Artifacts
import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import org.gradle.internal.extensions.stdlib.capitalized
import uk.gov.onelogin.sharing.plugins.PluginManagerExtensions.isAndroidApp
import uk.gov.onelogin.sharing.plugins.PluginManagerExtensions.isAndroidLibrary
import uk.gov.onelogin.sharing.plugins.PluginManagerExtensions.isJavaLibrary

plugins {
    id("jacoco")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

// Configures the gradle module based on whether it's an app module or a library module
if (pluginManager.isAndroidApp()) {
    configureAndroidApp()
} else if (pluginManager.isAndroidLibrary()) {
    configureAndroidLibrary()
}

/**
 * Enables test coverage when running the various test suites
 * Also defines the version of jacoco to utilise, as per the version catalog.
 */
fun CommonExtension<*, *, *, *, *, *>.configureTestCoverage() {
    testOptions {
        unitTests.all {
            testCoverage.jacocoVersion = libs.findVersion("jacoco").get().requiredVersion
        }
    }

    buildTypes {
        this.maybeCreate("debug").apply {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
        }
    }
}

/**
 * Registers the `combine${capitalisedVariant}JacocoReports` task to the android library gradle
 * module.
 *
 * This task uses the proceeding execution data files:
 * - `*.ec`: Emma coverage files generated from instrumentation tests
 * - `*.exec`: Jacoco execution data generated from unit tests
 *
 * The [LibraryAndroidComponentsExtension.onVariants] call provides the location of all compiled
 * class directories for a given library variant through the use of the
 * [LibraryVariant.artifacts] object's [Artifacts.forScope] function.
 *
 * @see LibraryAndroidComponentsExtension
 */
fun configureAndroidLibrary() {
    configure<LibraryExtension> {
        configureTestCoverage()
    }

    configure<LibraryAndroidComponentsExtension> {
        onVariants { variant ->
            val unusedInputJars: ListProperty<RegularFile> =
                objects.listProperty(RegularFile::class)
            // Hook into gradle lifecycle to obtain the latest version of compiled class directories
            val variantClassDirectories: ListProperty<Directory> =
                objects.listProperty(Directory::class)

            val combinedJacocoTask = registerAndroidJacocoTask(
                titleCaseVariant = variant.name.capitalized(),
                kotlinSources = variant.sources.kotlin?.all,
                variantClassDirectories = variantClassDirectories,
            )

            // Monitors the library variant's compiled classes to then pass the values
            // into `unusedInputJars` and `variantClassDirectories`.
            // This is to provide all of the directories containing compiled class files
            // that aren't instrumented by jacoco.
            variant.artifacts.forScope(ScopedArtifacts.Scope.PROJECT)
                .use(combinedJacocoTask)
                .toGet(
                    ScopedArtifact.CLASSES,
                    { unusedInputJars },
                    { variantClassDirectories },
                )
        }
    }
}

/**
 * Registers the `combine${capitalisedVariant}JacocoReports` task to the android app gradle
 * module.
 *
 * This task uses the proceeding execution data files:
 * - `*.ec`: Emma coverage files generated from instrumentation tests
 * - `*.exec`: Jacoco execution data generated from unit tests
 *
 * The [ApplicationAndroidComponentsExtension.onVariants] call provides the location of all compiled
 * class directories for a given library variant through the use of the
 * [ApplicationVariant.artifacts] object's [Artifacts.forScope] function.
 *
 * @see ApplicationAndroidComponentsExtension
 */
fun configureAndroidApp() {
    configure<ApplicationExtension> {
        configureTestCoverage()
    }

    configure<ApplicationAndroidComponentsExtension> {
        onVariants { variant ->
            val unusedInputJars: ListProperty<RegularFile> =
                objects.listProperty(RegularFile::class)
            // Hook into gradle lifecycle to obtain the latest version of compiled class directories
            val variantClassDirectories: ListProperty<Directory> =
                objects.listProperty(Directory::class)

            val combinedJacocoTask = registerAndroidJacocoTask(
                titleCaseVariant = variant.name.capitalized(),
                kotlinSources = variant.sources.kotlin?.all,
                variantClassDirectories = variantClassDirectories,
            )

            // Monitors the application variant's compiled classes to then pass the values
            // into `unusedInputJars` and `variantClassDirectories`.
            // This is to provide all of the directories containing compiled class files
            // that aren't instrumented by jacoco.
            variant.artifacts.forScope(ScopedArtifacts.Scope.PROJECT)
                .use(combinedJacocoTask)
                .toGet(
                    ScopedArtifact.CLASSES,
                    { unusedInputJars },
                    { variantClassDirectories },
                )
        }
    }
}

/**
 * Creates a custom [JacocoReport] gradle task for merging in all results from unit and
 * instrumentation tests for a given android gradle project.
 */
fun registerAndroidJacocoTask(
    titleCaseVariant: String,
    kotlinSources: Provider<out Collection<Directory>>?,
    variantClassDirectories: ListProperty<Directory>,
) = tasks.register<JacocoReport>(
    "combine${titleCaseVariant}JacocoReports"
) {
    group = "verification"
    description =
        "Merges all available jacoco data into a report in 'build/reports/coverage/combined'."
    configureJacocoExecutionData()

    kotlinSources?.let {
        this.sourceDirectories.setFrom(it)
    }

    classDirectories.setFrom(variantClassDirectories)

    configureJacocoReporting()
}

/**
 * Extension function for the [JacocoReport] gradle task, configuring the
 * [JacocoReport.reports] block.
 *
 * The task generates reports within the gradle module's `build/reports/coverage/combined`
 * directory.
 */
fun JacocoReport.configureJacocoReporting() {
    reports {
        csv.required = false
        html.outputLocation
        html.outputLocation.set(
            project.reporting.baseDirectory.dir(
                "coverage/combined"
            )
        )
        html.required = true
        xml.outputLocation.set(
            project.reporting.baseDirectory.file(
                "coverage/combined/report.xml"
            )
        )
        xml.required = true
    }
}

/**
 * Extension function for the [JacocoReport] gradle task, configuring the
 * [JacocoReport.executionData] property.
 *
 * The task monitors the gradle module's `build/outputs` folder for all files with the proceeding
 * extensions:
 * - `*.ec`: Emma coverage files generated from instrumentation tests
 * - `*.exec`: Jacoco coverage files generated from unit tests
 */
fun JacocoReport.configureJacocoExecutionData() {
    executionData.setFrom(
        layout.buildDirectory
            .dir("outputs")
            .map {
                it.asFileTree.matching {
                    include(
                        "**/*.ec",
                        "**/*.exec",
                    )
                }
            }
    )
}

tasks.withType(Test::class) {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}


