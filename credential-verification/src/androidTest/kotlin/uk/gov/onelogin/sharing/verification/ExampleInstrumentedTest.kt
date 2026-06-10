package uk.gov.onelogin.sharing.verification

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleInstrumentedTest {
    @Test
    fun deleteOnceMeaningfulInstrumentationTestsExist() {
        assertEquals(
            "uk.gov.onelogin.sharing.credentialVerification.test",
            InstrumentationRegistry.getInstrumentation().targetContext.packageName
        )
    }
}
