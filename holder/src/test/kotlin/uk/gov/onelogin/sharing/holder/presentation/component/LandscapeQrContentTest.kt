package uk.gov.onelogin.sharing.holder.presentation.component

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreenRule

@RunWith(AndroidJUnit4::class)
class LandscapeQrContentTest {

    @get:Rule
    val composeTestRule = HolderWelcomeScreenRule(createComposeRule())

    @Test
    fun `Shows screen content horizontally`() = composeTestRule.run {
        setContent { GdsTheme { LandscapeQrContent(qrData = "QR Data") } }
        composeTestRule.assertHeadingIsDisplayed()
        composeTestRule.assertQrCodeIsDisplayed()
        composeTestRule.assertBodyIsDisplayed()
        composeTestRule.assertLandscapeLayout()
    }
}
