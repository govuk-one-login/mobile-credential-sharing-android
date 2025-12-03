package uk.gov.onelogin.sharing.plugins

import java.io.FilenameFilter

/**
 * Wrapper object that contains regular expression exclusion patterns for file names.
 */
object Filters {
    val android =
        listOf(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "android/**/*.*",
            "**/*FileManager*",
            "**/*AndroidCamera*",
            "**/*AndroidBiometrics*",
            "**/*ContactsProvider*",
            "**/*IntentProvider*",
        )

    private val dataBindingFilters =
        listOf(
            "android/databinding/**/*.class",
            "**/android/databinding/*Binding.class",
            "**/android/databinding/*",
            "**/androidx/databinding/*",
            "**/databinding/*",
            "**/BR.*",
        )

    val kotlin =
        listOf(
            "**/*MapperImpl*.*",
            "**/*\$ViewInjector*.*",
            "**/*\$ViewBinder*.*",
            "**/BuildConfig.*",
            "**/*Component*.*",
            "**/*BR*.*",
            "**/Manifest*.*",
            "**/*\$Lambda$*.*",
            "**/*Companion*.*",
            "**/*MembersInjector*.*",
            "**/*_MembersInjector.class",
            "**/*_Factory*.*",
            "**/*_Provide*Factory*.*",
            "**/*Extensions*.*",
            "**/*Extension*.*",
            "**/*\$Result.*",
            "**/*\$Result$*.*",
        )

    val licenseFilters = listOf(
        "AL2.0",
        "LGPL2.1",
        "LICENSE.md",
        "LICENSE-notice.md",
    ).map {
        "META-INF/$it"
    }

    val sonar =
        listOf(
            "*.json",
            "**/.gradle/**",
            "**/*.gradle*",
        )

    // Dagger
    val dependencyInjectionFilter =
        listOf(
            "**/*_MembersInjector.class",
            "**/Dagger*Component.class",
            "**/Dagger*Component\$Builder.class",
            "**/Dagger*Subcomponent*.class",
            "**/*Subcomponent\$Builder.class",
            "**/*Module_*Factory.class",
            "**/dagger/hilt/internal/**/*.*",
            "**/di/module/*",
            "**/*_Factory*.*",
            "**/*Module*.*",
            "**/*Dagger*.*",
            "**/*Hilt*.*",
            "**/*DependenciesProvider*",
            "**/*_GeneratedInjector.*",
        )

    val navigationPluginFilter =
        listOf(
            "**/*Args.class",
            "**/*Directions.*",
        )

    val androidInstrumentationTests =
        listOf(
            android,
            dataBindingFilters,
            dependencyInjectionFilter,
            navigationPluginFilter,
            kotlin,
        ).flatten()

    val testSourceSets =
        listOf(
            "**/src/test/kotlin/\$",
            "**/src/test*/kotlin/\$",
            "**/src/androidTest*/kotlin/\$",
            "**/src/androidTest/kotlin/\$",
        )

    val developer =
        listOf(
            "**/src/*/java/**/developer/**",
            "**/src/*/kotlin/**/developer/**",
        )
}