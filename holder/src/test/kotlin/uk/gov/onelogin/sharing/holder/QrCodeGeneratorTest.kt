package uk.gov.onelogin.sharing.holder

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.verifier.QrCodeGenerator.INVALID_QR_CODE_DATA
import uk.gov.onelogin.sharing.verifier.QrCodeGenerator.QR_CODE_CONTENT_DESC
import uk.gov.onelogin.sharing.verifier.QrCodeGenerator.QR_CODE_DATA
import uk.gov.onelogin.sharing.verifier.QrCodeGenerator.QR_CODE_SIZE

@RunWith(RobolectricTestRunner::class)
class QrCodeGeneratorTest {

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
            .onNode(hasContentDescription(QR_CODE_CONTENT_DESC))
            .assertIsDisplayed()
    }

    @Test
    fun showsNoQrCodeWhenDataIsEmpty() {
        composeTestRule.setContent {
            QrCodeImage(
                modifier = Modifier.testTag(testTag),
                data = INVALID_QR_CODE_DATA,
                size = QR_CODE_SIZE
            )
        }
        composeTestRule
            .onNode(hasContentDescription(QR_CODE_CONTENT_DESC))
            .assertDoesNotExist()
    }

    @Test
    fun returnABitmapWhenDataNotNull() {
        val bitmap = qrCodeGenerator(data = QR_CODE_DATA, size = QR_CODE_SIZE)
        assertNotNull(bitmap)
    }

    @Test
    fun returnNullWhenDataIsNull() {
        val bitmap = qrCodeGenerator(data = INVALID_QR_CODE_DATA, size = QR_CODE_SIZE)
        assertNull(bitmap)
    }
}