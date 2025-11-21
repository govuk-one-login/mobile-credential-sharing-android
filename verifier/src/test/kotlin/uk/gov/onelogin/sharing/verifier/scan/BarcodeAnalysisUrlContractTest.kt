package uk.gov.onelogin.sharing.verifier.scan

import android.content.Intent
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.scan.BarcodeAnalysisUrlContractAssertions.hasState

@RunWith(AndroidJUnit4::class)
class BarcodeAnalysisUrlContractTest {

    private var resultCode = -1
    private var resultIntent: Intent? = null
    private val contract = BarcodeAnalysisUrlContract { code, intent ->
        resultCode = code
        resultIntent = intent
    }

    private val uri = "https://this.is.a.unit.test".toUri()

    @Test
    fun createsIntent() {
        val intent = contract.createIntent(
            context = ApplicationProvider.getApplicationContext(),
            input = uri
        )

        assertThat(
            intent,
            hasState(uri)
        )
    }

    @Test
    fun parsingJustPassesThrough() {
        contract.parseResult(resultCode = Int.MAX_VALUE, intent = Intent())

        assertEquals(
            Int.MAX_VALUE,
            resultCode
        )
        assertNotNull(resultIntent)
    }
}
