package uk.gov.onelogin.sharing.security

import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateValidKeyPair
import uk.gov.onelogin.sharing.security.cbor.dto.CoseKeyDto
import uk.gov.onelogin.sharing.security.cbor.dto.SecurityDto
import uk.gov.onelogin.sharing.security.cose.CoseKey

object SecurityDeserializerStub {

    private val keyPair = generateValidKeyPair()
    private val coseKey = CoseKey.generateCoseKey(keyPair!!)

    val expectedCoseKey = CoseKeyDto(
        keyType = 2,
        curve = 1,
        x = coseKey.x,
        y = coseKey.y
    )

    val expectedSecurityDto = SecurityDto(
        cipherSuiteIdentifier = 1,
        ephemeralPublicKey = expectedCoseKey
    )

    val coseKeyMap = mapOf(
        "1" to expectedCoseKey.keyType,
        "-1" to expectedCoseKey.curve,
        "-2" to expectedCoseKey.x,
        "-3" to expectedCoseKey.y
    )
}
