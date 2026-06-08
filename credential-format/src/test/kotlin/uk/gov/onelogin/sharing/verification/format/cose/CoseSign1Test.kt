package uk.gov.onelogin.sharing.verification.format.cose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CoseSign1Test {

    private val protectedHeader = byteArrayOf(0x01, 0x02)
    private val unprotectedHeader = byteArrayOf(0x03, 0x04)
    private val payload = byteArrayOf(0x05, 0x06)
    private val signature = byteArrayOf(0x07, 0x08)

    private val coseSign1 = CoseSign1(
        protectedHeader = protectedHeader,
        unprotectedHeader = unprotectedHeader,
        payload = payload,
        signature = signature
    )

    @Test
    fun `payload can be null for detached content`() {
        val detached = CoseSign1(
            protectedHeader = protectedHeader,
            unprotectedHeader = unprotectedHeader,
            payload = null,
            signature = signature
        )

        assertNull(detached.payload)
    }

    @Test
    fun `equality contract`() {
        val copy = CoseSign1(
            protectedHeader = protectedHeader.copyOf(),
            unprotectedHeader = unprotectedHeader.copyOf(),
            payload = payload.copyOf(),
            signature = signature.copyOf()
        )

        assertEquals(coseSign1, copy)
        assertFalse(coseSign1.equals("different type"))
        assertNotEquals(
            coseSign1,
            CoseSign1(byteArrayOf(0x0F), unprotectedHeader, payload, signature)
        )
        assertNotEquals(
            coseSign1,
            CoseSign1(protectedHeader, byteArrayOf(0x0F), payload, signature)
        )
        assertNotEquals(
            coseSign1,
            CoseSign1(protectedHeader, unprotectedHeader, byteArrayOf(0x0F), signature)
        )
        assertNotEquals(
            coseSign1,
            CoseSign1(protectedHeader, unprotectedHeader, payload, byteArrayOf(0x0F))
        )
    }

    @Test
    fun `hashCode contract`() {
        val copy = CoseSign1(
            protectedHeader = protectedHeader.copyOf(),
            unprotectedHeader = unprotectedHeader.copyOf(),
            payload = payload.copyOf(),
            signature = signature.copyOf()
        )

        assertEquals(coseSign1.hashCode(), copy.hashCode())
        assertNotEquals(
            coseSign1.hashCode(),
            CoseSign1(byteArrayOf(0x0F), unprotectedHeader, payload, signature).hashCode()
        )
    }
}
