package uk.gov.onelogin.sharing.holder.consent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.onelogin.sharing.holder.consent.AgreeToShareCardSamples.ORGANIZATION_NAME
import uk.gov.onelogin.sharing.holder.consent.AgreeToShareCardSamples.card

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xhdpi")
class AgreeToShareCardScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun capture(name: String, card: ConsentCard) {
        composeTestRule.setContent { Render(card) }
        composeTestRule.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    @Composable
    private fun Render(card: ConsentCard) {
        GdsTheme {
            AgreeToShareCard(
                card = card,
                organizationName = ORGANIZATION_NAME,
                modifier = Modifier
                    .background(Color.White)
                    .padding(spacingDouble)
            )
        }
    }

    @Test
    fun `no details kept variant`() {
        capture(
            name = "AgreeToShareCard_noDetailsKept",
            card = card(AgreeToShareCardSamples.noDetailsKept)
        )
    }

    @Test
    fun `all details kept variant`() {
        capture(
            name = "AgreeToShareCard_allDetailsKept",
            card = card(AgreeToShareCardSamples.allDetailsKept)
        )
    }

    @Test
    fun `some details kept variant`() {
        capture(
            name = "AgreeToShareCard_someDetailsKept",
            card = card(AgreeToShareCardSamples.someDetailsKept)
        )
    }
}
