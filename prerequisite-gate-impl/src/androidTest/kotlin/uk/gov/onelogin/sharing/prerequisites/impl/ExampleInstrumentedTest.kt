package uk.gov.onelogin.sharing.prerequisites.impl

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleInstrumentedTest {
    @Test
    fun deleteOnceMeaningfulInstrumentationTestsExist() {
        assertEquals(
            "uk.gov.onelogin.sharing.prerequisites.impl.test",
            InstrumentationRegistry.getInstrumentation().context.packageName
        )
    }
}
