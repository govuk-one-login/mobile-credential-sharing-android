package uk.gov.onelogin.sharing.testapp.credential

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayInputStream
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.junit.Assert.assertArrayEquals
import uk.gov.onelogin.sharing.testapp.R
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialData.mockCredentialState

class MockCredentialStateTest {
    private val base64EncodedCredential = "AQID"

    private val privateKeyContent = this::class.java.classLoader
        ?.getResourceAsStream("test_private_key.pem")
        ?.bufferedReader()
        ?.readText() ?: ""

    private val context: Context = mockk {
        every { resources } returns mockk<Resources> {
            every { openRawResource(R.raw.mock_credential) } returns
                ByteArrayInputStream(base64EncodedCredential.toByteArray())
        }
        every { assets } returns mockk<AssetManager> {
            every { open("test_private_key.pem") } returns
                ByteArrayInputStream(privateKeyContent.toByteArray())
        }
    }

    @Test
    fun `mockCredential equality contract`() {
        assertEquals(mockCredentialState, mockCredentialState.copy())
        assertNotEquals(mockCredentialState, mockCredentialState.copy(id = "Unit test"))
        assertNotEquals(mockCredentialState, mockCredentialState.copy(displayName = "Unit test"))
        assertNotEquals(
            mockCredentialState,
            mockCredentialState.copy(privateKeyAssetName = "Unit test")
        )
        assertNotEquals(mockCredentialState, mockCredentialState.copy(rawCredentialRes = -2))
    }

    @Test
    fun `mockCredential returns credential with decoded rawCredential`() {
        val credential = mockCredentialState.toCredential(context)
        assertArrayEquals(byteArrayOf(1, 2, 3), credential.rawCredential)
    }

    @Test
    fun `mockCredential returns credential with a valid UUID id`() {
        val credential = mockCredentialState.toCredential(context)
        assertEquals(UUID.fromString(credential.id).toString(), credential.id)
    }
}
