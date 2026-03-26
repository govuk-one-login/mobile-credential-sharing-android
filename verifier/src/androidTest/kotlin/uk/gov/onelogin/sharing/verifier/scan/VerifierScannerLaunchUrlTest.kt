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
import uk.gov.onelogin.sharing.cryptoService.DecoderStub
import uk.gov.onelogin.sharing.cryptoService.scanner.FakeQrParser
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs

@RunWith(AndroidJUnit4::class)
class VerifierScannerLaunchUrlTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val resources: Resources = context.resources

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
        val fakeOrchestrator = FakeOrchestrator(
            parser = FakeQrParser()
        )
        val model = VerifierScannerViewModel(orchestrator = fakeOrchestrator)

        composeTestRule.run {
            var hasNavigatedViaValidBarcode = false

            render(
                model = model,
                onValidBarcode = { hasNavigatedViaValidBarcode = true }
            )

            waitForIdle()

            model.orchestrator.processQrCode(
                DecoderStub.VALID_MDOC_URI
            )

            waitForIdle()

            Assert.assertTrue(hasNavigatedViaValidBarcode)
        }
    }

    @Test
    fun invalidUrlsDeferToOnInvalidBarcodeLambda() = runTest {
        val fakeOrchestrator = FakeOrchestrator(
            parser = FakeQrParser()
        )
        val model = VerifierScannerViewModel(orchestrator = fakeOrchestrator)

        composeTestRule.run {
            var hasNavigatedViaInvalidBarcode = false

            render(
                model = model,
                onInvalidBarcode = { hasNavigatedViaInvalidBarcode = true }
            )

            waitForIdle()

            fakeOrchestrator.processQrCode(
                BarcodeDataResultStubs.invalidBarcodeDataResultOne
            )

            waitForIdle()

            Assert.assertTrue(hasNavigatedViaInvalidBarcode)
        }
    }

    private fun render(
        model: VerifierScannerViewModel,
        onInvalidBarcode: (String) -> Unit = {},
        onValidBarcode: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            VerifierScanner(
                viewModel = model,
                onInvalidBarcode = onInvalidBarcode,
                onValidBarcode = onValidBarcode
            )
        }
    }
}
