package uk.gov.onelogin.sharing.testapp

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
