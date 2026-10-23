package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import uk.gov.android.ui.theme.m3.GdsTheme

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xhdpi")
class TurnOnBluetoothScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `Content variant`() {
        capture(name = "TurnOnBluetooth_content") {
            TurnOnBluetoothContent(onTurnOnBluetooth = {})
        }
    }

    @Test
    fun `Content dark variant`() {
        capture(name = "TurnOnBluetooth_contentDark", darkTheme = true) {
            TurnOnBluetoothContent(onTurnOnBluetooth = {})
        }
    }

    @Test
    fun `Content large font variant`() {
        capture(name = "TurnOnBluetooth_contentLargeFont", fontScale = 2f) {
            TurnOnBluetoothContent(onTurnOnBluetooth = {})
        }
    }

    @Test
    @Config(qualifiers = "+land")
    fun `Content landscape variant`() {
        capture(name = "TurnOnBluetooth_contentLandscape") {
            TurnOnBluetoothContent(onTurnOnBluetooth = {})
        }
    }

    @Test
    fun `Content loading variant`() {
        capture(name = "TurnOnBluetooth_loading") {
            TurnOnBluetoothLoadingContent()
        }
    }

    @Composable
    private fun Render(
        darkTheme: Boolean = false,
        fontScale: Float = 1f,
        content: @Composable () -> Unit
    ) {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
            GdsTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) { content() }
            }
        }
    }

    private fun capture(
        name: String,
        darkTheme: Boolean = false,
        fontScale: Float = 1f,
        content: @Composable () -> Unit
    ) {
        composeTestRule.setContent {
            Render(darkTheme = darkTheme, fontScale = fontScale) { content() }
        }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}
