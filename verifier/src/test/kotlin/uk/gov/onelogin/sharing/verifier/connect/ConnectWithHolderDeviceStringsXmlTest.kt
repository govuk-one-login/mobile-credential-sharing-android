package uk.gov.onelogin.sharing.verifier.connect

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.verifier.res.values.DeferredStringsXmlData
import uk.gov.onelogin.sharing.verifier.res.values.StringsXmlData

@RunWith(RobolectricTestParameterInjector::class)
class ConnectWithHolderDeviceStringsXmlTest {
    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @Test
    fun screenSpecificStringsDeferToBaseValues(
        @TestParameter input: ConnectWithHolderDeviceDeferredStringsXmlData
    ) = runTest {
        assertEquals(
            resources.getString(input.defersTo),
            resources.getString(input.resourceId)
        )
    }
}
