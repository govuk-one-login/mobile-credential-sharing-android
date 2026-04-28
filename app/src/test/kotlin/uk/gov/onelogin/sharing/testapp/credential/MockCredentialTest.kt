package uk.gov.onelogin.sharing.testapp.credential

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

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
        assertEquals(sut, sut.copy())
        assertNotEquals(sut, sut.copy(id = "other"))
        assertNotEquals(sut, sut.copy(displayName = "other"))
        assertNotEquals(sut, sut.copy(rawCredential = byteArrayOf()))
        assertNotEquals(sut, sut.copy(privateKey = byteArrayOf()))
    }

    @Test
    fun `String output doesn't leak private data`() {
        val asString = sut.toString()

        assertFalse { "privateKey" in asString }
        assertFalse { "rawCredential" in asString }
    }
}
