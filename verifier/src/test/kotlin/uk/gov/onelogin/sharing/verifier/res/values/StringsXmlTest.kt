package uk.gov.onelogin.sharing.verifier.res.values

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector

@RunWith(RobolectricTestParameterInjector::class)
class StringsXmlTest {
    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @Test
    fun baseStringsHaveSetValues(@TestParameter input: StringsXmlData) = runTest {
        assertEquals(
            input.expected,
            resources.getString(input.resourceId)
        )
    }

    @Test
    fun screenSpecificStringsDeferToBaseValues(@TestParameter input: DeferredStringsXmlData) =
        runTest {
            assertEquals(
                resources.getString(input.defersTo),
                resources.getString(input.resourceId)
            )
        }
}
