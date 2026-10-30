package uk.gov.onelogin.sharing.holder.res.values

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import org.robolectric.annotation.Config

@RunWith(RobolectricTestParameterInjector::class)
@Config(qualifiers = "cy")
class WelshStringsXmlTest {
    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @Test
    fun baseStringsHaveWelshValues(@TestParameter input: WelshStringsXmlData) = runTest {
        assertEquals(
            input.expected,
            resources.getString(input.resourceId)
        )
    }

    @Test
    fun screenSpecificStringsDeferToWelshBaseValues(@TestParameter input: DeferredStringsXmlData) =
        runTest {
            assertEquals(
                resources.getString(input.defersTo),
                resources.getString(input.resourceId)
            )
        }
}
