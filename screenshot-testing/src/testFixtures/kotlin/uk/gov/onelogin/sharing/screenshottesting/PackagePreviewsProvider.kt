package uk.gov.onelogin.sharing.screenshottesting

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * A [TestParameterValuesProvider] that provides every `@Preview` composable found in the same
 * package tree as the concrete subclass.
 *
 * To create a provider for a module, declare a subclass inside that module's test package so its
 * package name becomes the scan root:
 *
 * ```kt
 * object ModulePackagePreviewsProvider : PackagePreviewsProvider()
 * ```
 *
 * Multipreview annotations (`@PreviewLightDark`, `@PreviewFontScale`, locale/device `@Preview`s,
 * etc.) are expanded by the scanner into one [ComposablePreview] per configuration, so each shows
 * up as its own screenshot.
 */
abstract class PackagePreviewsProvider : TestParameterValuesProvider() {
    private val namespace = this::class.java.`package`!!.name

    override fun provideValues(context: Context?): List<ComposablePreview<AndroidPreviewInfo>> =
        AndroidComposablePreviewScanner()
            .scanPackageTrees(namespace)
            .getPreviews()
}
