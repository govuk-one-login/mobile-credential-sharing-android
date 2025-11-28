package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.security.DecoderStub.INVALID_CBOR
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.validBarcodeDataResult

@RunWith(RobolectricTestParameterInjector::class)
class ConnectWithHolderDeviceScreenTest {

    @get:Rule
    val composeTestRule = ConnectWithHolderDeviceRule(createComposeRule())

    @TestParameter(valuesProvider = ConnectWithHolderDeviceRenderProvider::class)
    lateinit var renderFunction: ConnectWithHolderDeviceRule.(String, Modifier) -> Unit

    @Test
    fun cannotDecodeProvidedCborString() = runTest {
        composeTestRule.run {
            renderFunction(INVALID_CBOR, Modifier)
            assertBasicInformationIsDisplayed(base64EncodedEngagement = INVALID_CBOR)
            assertErrorIsDisplayed()
            assertDeviceEngagementDataDoesNotExist()
        }
    }

    @Test
    fun validCborExistsOnScreen() = runTest {
        composeTestRule.run {
            renderFunction(validBarcodeDataResult.data, Modifier)
            assertBasicInformationIsDisplayed(base64EncodedEngagement = validBarcodeDataResult.data)
            assertErrorDoesNotExist()
            assertDeviceEngagementDataIsDisplayed()
        }
    }
}
