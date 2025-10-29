package uk.gov.onelogin.sharing.holder

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.verifier.FakeEngagementGenerator
import uk.gov.onelogin.sharing.verifier.QrCodeGenerator.INVALID_QR_CODE_DATA
import uk.gov.onelogin.sharing.verifier.QrCodeGenerator.QR_CODE_CONTENT_DESC
import uk.gov.onelogin.sharing.verifier.QrCodeGenerator.QR_CODE_DATA
import uk.gov.onelogin.sharing.verifier.QrCodeGenerator.QR_CODE_SIZE

@RunWith(RobolectricTestRunner::class)
class QrCodeImageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testTag = "qrCode"

    @Test
    fun showsQrCode() {
        composeTestRule.setContent {
            QrCodeImage(
                modifier = Modifier.testTag(testTag),
                data = QR_CODE_DATA,
                size = QR_CODE_SIZE
            )
        }

        composeTestRule
            .onNodeWithTag(testTag)
            .assertIsDisplayed()
            .assertContentDescriptionEquals(QR_CODE_CONTENT_DESC)
            .assertHeightIsEqualTo(QR_CODE_SIZE.dp)
            .assertWidthIsEqualTo(QR_CODE_SIZE.dp)
            .assertIsDisplayed()
    }

    @Test
    fun showsNoQrCodeWhenDataIsEmpty() {
        composeTestRule.setContent {
            QrCodeImage(
                data = INVALID_QR_CODE_DATA,
                size = QR_CODE_SIZE
            )
        }
        composeTestRule
            .onNode(hasContentDescription(QR_CODE_CONTENT_DESC))
            .assertDoesNotExist()
    }

    @Test
    fun displaysQrCodeWithBase64EngagementCode() {
        val engagementGenerator = FakeEngagementGenerator()
        composeTestRule.setContent {
            QrCodeImage(
                modifier = Modifier.testTag(testTag),
                data = engagementGenerator.generateEncodedBase64QrEngagement(),
                size = QR_CODE_SIZE
            )
        }

        composeTestRule
            .onNodeWithTag(testTag)
            .assertIsDisplayed()
            .assertContentDescriptionEquals(QR_CODE_CONTENT_DESC)
            .assertHeightIsEqualTo(QR_CODE_SIZE.dp)
            .assertWidthIsEqualTo(QR_CODE_SIZE.dp)
            .assertIsDisplayed()
    }
}
