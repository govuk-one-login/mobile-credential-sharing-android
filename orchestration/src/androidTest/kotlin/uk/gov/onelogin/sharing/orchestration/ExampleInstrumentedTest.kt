package uk.gov.onelogin.sharing.orchestration

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleInstrumentedTest {
    @Test
    fun deleteOnceMeaningfulInstrumentationTestsExist() {
        assertEquals(
            "uk.gov.onelogin.sharing.orchestration.test",
            InstrumentationRegistry.getInstrumentation().context.packageName
        )
    }
}
