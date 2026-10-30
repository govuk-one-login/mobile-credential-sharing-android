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
    fun translatableStringsMatchEnglishValues(@TestParameter input: TranslatableStringsData) =
        runTest {
            assertEquals(
                input["en"],
                resources.getString(input.resourceId)
            )
        }

    @Config(qualifiers = "cy")
    @Test
    fun translatableStringsMatchWelshValues(@TestParameter input: TranslatableStringsData) =
        runTest {
            assertEquals(
                input["cy"],
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
