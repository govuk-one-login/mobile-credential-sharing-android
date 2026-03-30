package uk.gov.onelogin.sharing.testapp

import android.content.Context
import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayInputStream
import java.util.UUID
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockCredentialTest {

    private val id = "test-id"
    private val displayName = "Jane Doe"
    private val rawCredential = byteArrayOf(1, 2, 3)
    private val privateKey = byteArrayOf(4, 5, 6)

    private val sut = MockCredential(
        id = id,
        displayName = displayName,
        rawCredential = rawCredential,
        privateKey = privateKey
    )

    @Test
    fun `properties are set correctly`() {
        assertEquals(id, sut.id)
        assertEquals(displayName, sut.displayName)
        assertArrayEquals(rawCredential, sut.rawCredential)
        assertArrayEquals(privateKey, sut.privateKey)
    }

    @Test
    fun `equals returns true for identical instances`() {
        val other = MockCredential(id, displayName, rawCredential.copyOf(), privateKey.copyOf())
        assertEquals(sut, other)
    }

    @Test
    fun `equals returns false when id differs`() {
        val other = sut.copy(id = "different-id")
        assertNotEquals(sut, other)
    }

    @Test
    fun `equals returns false when displayName differs`() {
        val other = sut.copy(displayName = "John Doe")
        assertNotEquals(sut, other)
    }

    @Test
    fun `equals returns false when rawCredential differs`() {
        val other = sut.copy(rawCredential = byteArrayOf(9, 9, 9))
        assertNotEquals(sut, other)
    }

    @Test
    fun `equals returns false when privateKey differs`() {
        val other = sut.copy(privateKey = byteArrayOf(9, 9, 9))
        assertNotEquals(sut, other)
    }

    @Test
    fun `equals returns false for non-MockCredential type`() {
        assertFalse(sut.equals("not a MockCredential"))
    }

    @Test
    fun `equals returns true for same reference`() {
        assertTrue(sut.equals(sut))
    }

    @Test
    fun `hashCode is equal for identical instances`() {
        val other = MockCredential(id, displayName, rawCredential.copyOf(), privateKey.copyOf())
        assertEquals(sut.hashCode(), other.hashCode())
    }

    @Test
    fun `hashCode differs when fields differ`() {
        assertNotEquals(sut.hashCode(), sut.copy(id = "other").hashCode())
        assertNotEquals(sut.hashCode(), sut.copy(displayName = "other").hashCode())
        assertNotEquals(sut.hashCode(), sut.copy(rawCredential = byteArrayOf(9)).hashCode())
        assertNotEquals(sut.hashCode(), sut.copy(privateKey = byteArrayOf(9)).hashCode())
    }
}

class MockCredentialsTest {

    // Base64 URL-safe encoding of byteArrayOf(1, 2, 3) with no padding
    private val base64EncodedCredential = "AQID"

    private val context: Context = mockk {
        every { resources } returns mockk<Resources> {
            every { openRawResource(R.raw.mock_credential) } returns
                ByteArrayInputStream(base64EncodedCredential.toByteArray())
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
        // Throws IllegalArgumentException if not a valid UUID
        UUID.fromString(credential.id)
    }

    @Test
    fun `mockCredential returns a unique id on each call`() {
        every { context.resources.openRawResource(R.raw.mock_credential) } returnsMany listOf(
            ByteArrayInputStream(base64EncodedCredential.toByteArray()),
            ByteArrayInputStream(base64EncodedCredential.toByteArray())
        )

        val first = MockCredentials.mockCredential(context)
        val second = MockCredentials.mockCredential(context)

        assertNotEquals(first.id, second.id)
    }
}
