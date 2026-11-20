package uk.gov.onelogin.sharing.holder.qrcode.component

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.holder.qrcode.QrCodeScreenRule

@RunWith(AndroidJUnit4::class)
class LandscapeQrContentTest {

    @get:Rule
    val composeTestRule = QrCodeScreenRule(createComposeRule())

    @Test
    @Config(qualifiers = "w891dp-h411dp-land")
    fun `Shows screen content horizontally`() = composeTestRule.run {
        setContent { GdsTheme { LandscapeQrContent(qrPayload = "QR Data") } }
        composeTestRule.assertHeadingIsDisplayed()
        composeTestRule.assertQrCodeIsDisplayed()
        composeTestRule.assertBodyIsDisplayed()
        composeTestRule.assertLandscapeLayout()
    }
}
