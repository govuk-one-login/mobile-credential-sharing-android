package uk.gov.onelogin.sharing.security

import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateValidPublicKey
import uk.gov.onelogin.sharing.security.cbor.dto.CoseKeyDto
import uk.gov.onelogin.sharing.security.cbor.dto.SecurityDto
import uk.gov.onelogin.sharing.security.cbor.encodeCbor
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.security.cose.CoseKey

object SecurityDeserializerStub {

    private val logger = SystemLogger()
    private val keyPair = generateValidPublicKey()
    val validCoseKey = CoseKey.generateCoseKey(keyPair, logger)
    val embeddedCoseKey = EmbeddedCbor(validCoseKey.encodeCbor())

    val expectedCoseKey = CoseKeyDto(
        keyType = 2,
        curve = 1,
        x = validCoseKey.x,
        y = validCoseKey.y
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
