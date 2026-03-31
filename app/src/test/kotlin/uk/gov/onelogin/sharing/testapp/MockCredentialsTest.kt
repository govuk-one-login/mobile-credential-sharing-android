package uk.gov.onelogin.sharing.testapp

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayInputStream
import java.util.UUID
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class MockCredentialsTest {
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
    fun `mockCredential returns credential with displayName Jane Doe`() {
        val credential = MockCredentials.mockCredential(context)
        assertEquals("Jane Doe", credential.displayName)
    }

    @Test
    fun `mockCredential returns credential with decoded rawCredential`() {
        val credential = MockCredentials.mockCredential(context)
        assertArrayEquals(byteArrayOf(1, 2, 3), credential.rawCredential)
    }

    @Test
    fun `mockCredential returns credential with a valid UUID id`() {
        val credential = MockCredentials.mockCredential(context)
        assertEquals(UUID.fromString(credential.id).toString(), credential.id)
    }

    @Test
    fun `mockCredential returns a unique id on each call`() {
        every { context.resources.openRawResource(R.raw.mock_credential) } returnsMany listOf(
            ByteArrayInputStream(base64EncodedCredential.toByteArray()),
            ByteArrayInputStream(base64EncodedCredential.toByteArray())
        )
        every { context.assets.open("test_private_key.pem") } returnsMany listOf(
            ByteArrayInputStream(privateKeyContent.toByteArray()),
            ByteArrayInputStream(privateKeyContent.toByteArray())
        )

        val first = MockCredentials.mockCredential(context)
        val second = MockCredentials.mockCredential(context)

        assertNotEquals(first.id, second.id)
    }
}
