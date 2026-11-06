package uk.gov.onelogin.sharing.holder.presentation.component

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreenRule

@RunWith(AndroidJUnit4::class)
class HolderWelcomeQrCodeTest {

    @get:Rule
    val composeTestRule = HolderWelcomeScreenRule(createComposeRule())

    @Test
    fun `QR code fills given space`() {
        composeTestRule.setContent {
            HolderWelcomeQrCode(qrData = "QR Data", modifier = Modifier.size(250.dp))
        }
        val expectedQrCodeSize = 250.dp
        composeTestRule.assertQrCodeIsDisplayed()
        composeTestRule.assertQrCodeSizeIs(expectedQrCodeSize)
    }
}
