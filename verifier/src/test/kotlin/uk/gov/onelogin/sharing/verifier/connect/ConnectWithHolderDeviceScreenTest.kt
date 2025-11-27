package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.security.DecoderStub.INVALID_CBOR
import uk.gov.onelogin.sharing.security.DecoderStub.VALID_CBOR

@RunWith(AndroidJUnit4::class)
class ConnectWithHolderDeviceScreenTest {

    @get:Rule
    val composeTestRule = ConnectWithHolderDeviceRule(createComposeRule())

    @Test
    fun cannotDecodeProvidedCborString() = runTest {
        composeTestRule.run {
            render(INVALID_CBOR)
            assertBasicInformationIsDisplayed(base64EncodedEngagement = INVALID_CBOR)
            assertErrorIsDisplayed()
            assertDeviceEngagementDataDoesNotExist()
        }
    }

    @Test
    fun validCborExistsOnScreen() = runTest {
        composeTestRule.run {
            render(VALID_CBOR)
            assertBasicInformationIsDisplayed(base64EncodedEngagement = VALID_CBOR)
            assertErrorDoesNotExist()
            assertDeviceEngagementDataIsDisplayed()
        }
    }
}
