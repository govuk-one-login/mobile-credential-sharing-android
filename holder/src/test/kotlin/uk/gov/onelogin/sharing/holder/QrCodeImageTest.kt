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
import java.util.UUID
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.holder.EngagementGeneratorStub.Companion.BASE64_ENCODED_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.holder.QrCodeGenerator.INVALID_QR_CODE_DATA
import uk.gov.onelogin.sharing.holder.QrCodeGenerator.QR_CODE_CONTENT_DESC
import uk.gov.onelogin.sharing.holder.QrCodeGenerator.QR_CODE_DATA
import uk.gov.onelogin.sharing.holder.QrCodeGenerator.QR_CODE_SIZE
import uk.gov.onelogin.sharing.holder.engagement.Engagement
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateValidKeyPair
import uk.gov.onelogin.sharing.security.cose.CoseKey

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
        val uuid = UUID.randomUUID()
        val mdocUri = Engagement { _, _ -> BASE64_ENCODED_DEVICE_ENGAGEMENT }

        composeTestRule.setContent {
            val key = generateValidKeyPair()
            val coseKey = CoseKey.generateCoseKey(key!!)

            QrCodeImage(
                modifier = Modifier.testTag(testTag),
                data = "mdoc:${mdocUri.qrCodeEngagement(coseKey, uuid)}",
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
