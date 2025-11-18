package uk.gov.onelogin.sharing.verifier

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleInstrumentedTest {
    @Test
    fun deleteOnceMeaningfulInstrumentationTestsExist() {
        assertEquals(
            "uk.gov.onelogin.sharing.verifier.test",
            InstrumentationRegistry.getInstrumentation().targetContext.packageName
        )
    }
}
