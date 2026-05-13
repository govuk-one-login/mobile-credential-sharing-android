package uk.gov.android.credentialsharing.iso18013_6

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleInstrumentedTest {
    @Test
    fun deleteOnceMeaningfulInstrumentationTestsExist() {
        assertEquals(
            "uk.gov.onelogin.sharing.iso18013_6_tests.test",
            InstrumentationRegistry.getInstrumentation().targetContext.packageName
        )
    }
}
