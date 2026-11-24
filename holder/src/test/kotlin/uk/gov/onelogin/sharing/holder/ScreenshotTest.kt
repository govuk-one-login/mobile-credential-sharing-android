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
) : BaseRoborazziScreenshotTest(preview) {

    /**
     * Previews containing an indeterminate progress indicator never become idle, so they are
     * captured with a paused Compose clock instead.
     */
    override val manualClockPreviewClassSimpleNames: Set<String> = setOf(
        "HolderConsentScreenKt",
        "HolderPrerequisitesScreenKt"
    )
}
