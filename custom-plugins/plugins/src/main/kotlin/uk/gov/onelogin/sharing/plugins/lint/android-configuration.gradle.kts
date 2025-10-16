package uk.gov.onelogin.sharing.plugins.lint

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint

if (pluginManager.hasPlugin("com.android.application")) {
    configure<ApplicationExtension> {
        lint {
            configureAndroidLinting()
        }
    }
} else if (pluginManager.hasPlugin("com.android.library")) {
    configure<LibraryExtension> {
        lint {
            configureAndroidLinting()
        }
    }
}

fun Lint.configureAndroidLinting() {
    abortOnError = true
    checkAllWarnings = true
    ignoreWarnings = false
    checkDependencies = true
    lintConfig = rootProject.layout.projectDirectory.file("android-lint-baseline.xml").asFile
}