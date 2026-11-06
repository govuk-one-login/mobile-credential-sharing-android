package uk.gov.onelogin.sharing.holder.presentation.component

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreenRule

@RunWith(AndroidJUnit4::class)
class PortraitQrContentTest {

    @get:Rule
    val composeTestRule = HolderWelcomeScreenRule(createComposeRule())

    @Test
    fun `Shows screen content stack vertically`() = composeTestRule.run {
        setContent { GdsTheme { PortraitQrContent(qrData = "QR Data") } }
        composeTestRule.assertHeadingIsDisplayed()
        composeTestRule.assertQrCodeIsDisplayed()
        composeTestRule.scrollToAndAssertBodyIsDisplayed()
        composeTestRule.assertPortraitLayout()
    }
}
