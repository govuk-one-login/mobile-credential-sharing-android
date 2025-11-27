package uk.gov.onelogin.sharing.verifier.scan

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs

@RunWith(AndroidJUnit4::class)
class VerifierScannerLaunchUrlTest {

    private val model = VerifierScannerViewModel()
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant()

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @get:Rule
    val composeTestRule = VerifierScannerRule(
        resources = resources,
        composeTestRule = createComposeRule()
    )

    @Test
    fun validUrlsDeferToOnValidBarcodeLambda() = runTest {
        composeTestRule.run {
            var hasNavigatedViaValidBarcode = false
            model.update(BarcodeDataResultStubs.validBarcodeDataResult)
            render(
                model,
                onValidBarcode = { hasNavigatedViaValidBarcode = true }
            )
            testScheduler.advanceUntilIdle()

            Assert.assertTrue(hasNavigatedViaValidBarcode)
        }
    }

    @Test
    fun invalidUrlsDeferToOnInvalidBarcodeLambda() = runTest {
        composeTestRule.run {
            var hasNavigatedViaInvalidBarcode = false
            model.update(BarcodeDataResultStubs.invalidBarcodeDataResultOne)
            render(
                model,
                onInvalidBarcode = { hasNavigatedViaInvalidBarcode = true }
            )
            testScheduler.advanceUntilIdle()

            Assert.assertTrue(hasNavigatedViaInvalidBarcode)
        }
    }
}
