package uk.gov.onelogin.sharing.screenshottesting

import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.toRoborazziComposeOptions
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * Base class for preview-driven Roborazzi screenshot tests.
 *
 * Extend this and pair it with a module-local [PackagePreviewsProvider] to screenshot every
 * `@Preview` in the module. Each discovered [ComposablePreview] (including each variant expanded
 * from a multi preview such as `@PreviewLightDark`) is rendered using the configuration declared on
 * the preview itself — ui mode (light/dark), locale, device and font scale — so the whole matrix is
 * controlled from the preview annotations rather than hand-written test code.
 *
 * #### Example usage
 *
 * ```kt
 * private object ModulePackagePreviewsProvider : PackagePreviewsProvider()
 *
 * class ScreenshotTest(
 *     @TestParameter(valuesProvider = ModulePackagePreviewsProvider::class)
 *     preview: ComposablePreview<AndroidPreviewInfo>,
 * ) : BaseRoborazziScreenshotTest(preview)
 * ```
 *
 * Baselines are written under `src/test/snapshots/<module-relative-package>/<name>.png`
 * (the common `uk.gov.onelogin.sharing` prefix is stripped so folders mirror the package tree).
 */
@RunWith(RobolectricTestParameterInjector::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class)
abstract class BaseRoborazziScreenshotTest(
    private val preview: ComposablePreview<AndroidPreviewInfo>
) {

    @Test
    fun screenshot() {
        preview.captureRoboImage(
            filePath = screenshotFilePath(),
            roborazziOptions = RoborazziOptions(),
            roborazziComposeOptions = preview.toRoborazziComposeOptions()
        )
    }

    private fun screenshotFilePath(): String {
        val packagePath = preview.declaringClass
            .substringBeforeLast('.')
            .removePrefix("$PACKAGE_ROOT.")
            .replace('.', '/')
        val className = preview.declaringClass.substringAfterLast('.')
        val configId = AndroidPreviewScreenshotIdBuilder(preview)
            .ignoreClassName()
            .ignoreMethodName()
            .build()
        val name = "${className}_${preview.methodName}_$configId"
        return "src/test/snapshots/$packagePath/$name.png"
    }

    private companion object {
        /** Common package prefix stripped from snapshot folder paths to keep them module-relative. */
        const val PACKAGE_ROOT = "uk.gov.onelogin.sharing"
    }
}
