package uk.gov.android.credentialsharing.iso180136

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentationTest {
    @Test
    fun deleteOnceMeaningfulInstrumentationTestsExist() {
        assertEquals(
            "uk.gov.onelogin.sharing.iso18013_6_tests.test",
            InstrumentationRegistry.getInstrumentation().targetContext.packageName
        )
    }
}
