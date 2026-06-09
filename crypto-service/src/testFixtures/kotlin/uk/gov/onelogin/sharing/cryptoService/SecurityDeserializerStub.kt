package uk.gov.onelogin.sharing.cryptoService

import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.SessionSecurityTestStub.generateValidPublicKey
import uk.gov.onelogin.sharing.cryptoService.cose.CoseKey
import uk.gov.onelogin.sharing.cryptoService.cose.toDto
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.security.CoseKeyDto
import uk.gov.onelogin.sharing.models.mdoc.security.SecurityDto
import uk.gov.onelogin.sharing.models.mdoc.security.toDto

object SecurityDeserializerStub {

    private val logger = SystemLogger()
    private val keyPair = generateValidPublicKey()
    val validCoseKey = CoseKey.generateCoseKey(keyPair, logger)
    val embeddedCoseKey = EmbeddedCbor(validCoseKey.toDto().toCbor())

    val expectedCoseKey = CoseKeyDto(
        keyType = 2,
        curve = 1,
        x = validCoseKey.x,
        y = validCoseKey.y
    )

    val expectedSecurityDto = SecurityDto(
        cipherSuiteIdentifier = 1,
        eDeviceKeyBytes = validCoseKey.toDto().toCbor(),
        ephemeralPublicKey = expectedCoseKey
    )
}
