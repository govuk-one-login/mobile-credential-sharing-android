package uk.gov.onelogin.sharing.security.engagement

import java.util.UUID
import uk.gov.onelogin.sharing.security.DecoderStub.VALID_CBOR
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateValidKeyPair
import uk.gov.onelogin.sharing.security.cose.CoseKey

/**
 * Test fake to be used by other classes if required
 *
 * */

object EngagementGeneratorStub {
    const val BASE64_ENCODED_DEVICE_ENGAGEMENT =
        "v2EwYzEuMGExnwHYGFBGQUtFX0VERVZJQ0VfS0VZ/2Eyn58CAb9hMPVhMfRiMTDYGFgkMTExMTExMTEtMjIyMi0zMzMzLTQ0NDQtNTU1NTU1NTU1NTU1/////w=="

    val qrCodeEngagementUuid: UUID = UUID.randomUUID()

    val fakeEngagement: Engagement = Engagement { _, _ ->
        BASE64_ENCODED_DEVICE_ENGAGEMENT
    }
    val encodedEngagement: String = fakeEngagement.qrCodeEngagement(
        key = CoseKey.generateCoseKey(generateValidKeyPair()!!),
        uuid = qrCodeEngagementUuid
    )
}
