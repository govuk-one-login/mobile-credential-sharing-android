package uk.gov.onelogin.sharing.security.dto

import java.security.interfaces.ECPublicKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateValidKeyPair
import uk.gov.onelogin.sharing.security.cbor.dto.CoseKeyDto
import uk.gov.onelogin.sharing.security.cose.Cose
import uk.gov.onelogin.sharing.security.cose.CoseKey

class CoseKeyDtoTest {

    private lateinit var genKey: ECPublicKey
    private lateinit var coseKey: CoseKey

    @Before
    fun setup() {
        genKey = generateValidKeyPair()!!
        coseKey = CoseKey.generateCoseKey(genKey)
    }

    @Test
    fun `equals should return true for instances with same values`() {
        val genKey = generateValidKeyPair()
        val coseKey = CoseKey.generateCoseKey(genKey!!)

        val dto1 = CoseKeyDto(
            keyType = Cose.KEY_TYPE_EC2,
            curve = Cose.EC_CURVE_LABEL,
            x = coseKey.x,
            y = coseKey.y
        )
        val dto2 = CoseKeyDto(
            keyType = Cose.KEY_TYPE_EC2,
            curve = Cose.EC_CURVE_LABEL,
            x = coseKey.x,
            y = coseKey.y
        )
        assertEquals(dto1, dto2)
    }

    @Test
    fun `equals should return false for instances with different values`() {
        val dto1 = CoseKeyDto(
            keyType = Cose.KEY_TYPE_EC2,
            curve = Cose.CURVE_P256,
            x = coseKey.x,
            y = coseKey.y
        )
        val dto2 = CoseKeyDto(
            keyType = Cose.KEY_TYPE_EC2,
            curve = Cose.EC_CURVE_LABEL,
            x = coseKey.x,
            y = coseKey.y
        )
        assertNotEquals(dto1, dto2)
    }

    @Test
    fun `hashCodes should be equal for instances`() {
        val dto1 = CoseKeyDto(
            keyType = Cose.KEY_TYPE_EC2,
            curve = Cose.EC_CURVE_LABEL,
            x = coseKey.x,
            y = coseKey.y
        )
        val dto2 = CoseKeyDto(
            keyType = Cose.KEY_TYPE_EC2,
            curve = Cose.EC_CURVE_LABEL,
            x = coseKey.x,
            y = coseKey.y
        )
        assertEquals(dto1.hashCode(), dto2.hashCode())
    }

    @Test
    fun `hashCodes should be different for non-equal instances`() {
        val dto1 = CoseKeyDto(
            keyType = Cose.KEY_TYPE_EC2,
            curve = Cose.CURVE_P256,
            x = coseKey.x,
            y = coseKey.y
        )
        val dto2 = CoseKeyDto(
            keyType = Cose.KEY_TYPE_EC2,
            curve = Cose.EC_CURVE_LABEL,
            x = coseKey.x,
            y = coseKey.y
        )
        assertNotEquals(dto1.hashCode(), dto2.hashCode())
    }
}
