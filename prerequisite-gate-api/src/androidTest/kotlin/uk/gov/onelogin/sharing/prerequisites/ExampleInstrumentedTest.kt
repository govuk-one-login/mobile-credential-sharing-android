package uk.gov.onelogin.sharing.prerequisites

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleInstrumentedTest {
    @Test
    fun deleteOnceMeaningfulInstrumentationTestsExist() {
        assertEquals(
            "uk.gov.onelogin.sharing.prerequisites.test",
            InstrumentationRegistry.getInstrumentation().context.packageName
        )
    }
}
