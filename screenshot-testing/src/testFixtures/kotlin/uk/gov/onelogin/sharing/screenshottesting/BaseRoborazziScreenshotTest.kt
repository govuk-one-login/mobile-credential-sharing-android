package uk.gov.onelogin.sharing.screenshottesting

import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.manualAdvance
import com.github.takahirom.roborazzi.toRoborazziComposeOptions
import org.junit.Rule
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

    /**
     * Simple names of `*Kt` preview-holder classes whose previews must be captured with the Compose
     * clock paused (`mainClock.autoAdvance = false`) rather than waiting for idle.
     *
     * Use this for screens whose content animates indefinitely — e.g. an indeterminate
     * [androidx.compose.material3.CircularProgressIndicator] (as in the GDS progress indicator) —
     * which otherwise never lets `captureRoboImage` reach idle and hangs the test. The frame is
     * advanced by [MANUAL_CLOCK_ADVANCE_MS] so one-shot effects run, then captured statically.
     *
     * This is opt-in per class: every other preview continues to use the default idle-wait capture
     * path unchanged, so existing baselines are unaffected.
     */
    protected open val manualClockPreviewClassSimpleNames: Set<String> = emptySet()

    private val useManualClock: Boolean
        get() = preview.declaringClass.substringAfterLast('.') in manualClockPreviewClassSimpleNames

    /**
     * A [ComposeContentTestRule] is only required for the manual-clock capture path. It is applied
     * conditionally so that the default (idle-wait) capture path — used by every non-opt-in preview
     * — runs in exactly the same environment as before, keeping those baselines unchanged.
     */
    @get:Rule
    val composeRule = ConditionalComposeRule { useManualClock }

    @Test
    fun screenshot() {
        val composeOptions = if (useManualClock) {
            preview.toRoborazziComposeOptions()
                .builder()
                .manualAdvance(composeRule.delegate, advanceTimeMillis = MANUAL_CLOCK_ADVANCE_MS)
                .build()
        } else {
            preview.toRoborazziComposeOptions()
        }

        preview.captureRoboImage(
            filePath = screenshotFilePath(),
            roborazziOptions = RoborazziOptions(),
            roborazziComposeOptions = composeOptions
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

        /**
         * Virtual time to advance the paused Compose clock by before capturing a manual-clock
         * preview — long enough for one-shot effects to run, short enough to land on a stable,
         * representative frame of any looping animation.
         */
        const val MANUAL_CLOCK_ADVANCE_MS = 1_000L
    }
}
