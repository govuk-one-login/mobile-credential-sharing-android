package uk.gov.onelogin.sharing.screenshottesting

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

// Placeholder so the `androidTest` source set runs a test (required by the coverage setup).
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun deleteOnceMeaningfulInstrumentationTestsExist() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("uk.gov.onelogin.sharing.screenshottesting.test", appContext.packageName)
    }
}
