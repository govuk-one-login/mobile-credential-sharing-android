package uk.gov.onelogin.sharing.ui.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun deleteOnceMeaningfulInstrumentationTestsExist() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("uk.gov.onelogin.sharing.uiimpl.test", appContext.packageName)
    }
}
