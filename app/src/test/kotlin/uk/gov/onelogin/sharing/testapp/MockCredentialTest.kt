package uk.gov.onelogin.sharing.testapp

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
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
}
