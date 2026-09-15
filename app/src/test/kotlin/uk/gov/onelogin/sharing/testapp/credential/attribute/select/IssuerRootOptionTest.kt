package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector

@RunWith(RobolectricTestParameterInjector::class)
class IssuerRootOptionTest(@TestParameter val option: IssuerRootOption) {
    @Test
    fun `Certificate asset is the der form of the root certificate asset`() = runTest {
        assertEquals(
            "${option.rootCertificateAsset}.der",
            option.certificateAsset
        )
    }

    @Test
    fun `Certificate asset uses the der extension`() = runTest {
        assertTrue(option.certificateAsset.endsWith(".der"))
    }

    @Test
    fun `Display name is not blank`() = runTest {
        assertTrue(option.displayName.isNotBlank())
    }
}
