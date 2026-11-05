package uk.gov.onelogin.sharing.holder

import com.google.testing.junit.testparameterinjector.TestParameter
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import uk.gov.onelogin.sharing.screenshottesting.BaseRoborazziScreenshotTest
import uk.gov.onelogin.sharing.screenshottesting.PackagePreviewsProvider

private object ModulePackagePreviewsProvider : PackagePreviewsProvider()

class ScreenshotTest(
    @TestParameter(valuesProvider = ModulePackagePreviewsProvider::class)
    preview: ComposablePreview<AndroidPreviewInfo>
) : BaseRoborazziScreenshotTest(preview)
