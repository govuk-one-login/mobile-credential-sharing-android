package uk.gov.onelogin.sharing.plugins.lint

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import uk.gov.onelogin.sharing.plugins.PluginManagerExtensions.isAndroidApp
import uk.gov.onelogin.sharing.plugins.PluginManagerExtensions.isAndroidLibrary

if (pluginManager.isAndroidApp()) {
    configure<ApplicationExtension> {
        lint {
            configureAndroidLinting()
        }
    }
} else if (pluginManager.isAndroidLibrary()) {
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