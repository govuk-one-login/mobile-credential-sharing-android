package uk.gov.onelogin.sharing.verifier.connect.error

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector

@RunWith(RobolectricTestParameterInjector::class)
class BluetoothConnectionErrorStringsXmlTest {
    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @Test
    fun screenSpecificStringsDeferToBaseValues(
        @TestParameter input: BluetoothConnectionErrorDeferredStringsXmlData
    ) = runTest {
        assertEquals(
            resources.getString(input.defersTo),
            resources.getString(input.resourceId)
        )
    }
}
