package uk.gov.onelogin.sharing.cryptoService.engagement

import org.junit.Assert.assertNotNull
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.SessionSecurityTestStub
import uk.gov.onelogin.sharing.cryptoService.cose.CoseKey
import uk.gov.onelogin.sharing.cryptoService.engagement.EngagementGeneratorStub.qrCodeEngagementUuid

class EngagementGeneratorTest {

    private val logger = SystemLogger()
    private val engagementGenerator: EngagementGenerator = EngagementGenerator(
        logger = logger
    )

    @Test
    fun `generates base 64 encoded string for device engagement`() {
        val key = SessionSecurityTestStub.generateValidPublicKey()
        val coseKey = CoseKey.generateCoseKey(key, logger)

        val engagementString = engagementGenerator.qrCodeEngagement(coseKey, qrCodeEngagementUuid)
        assertNotNull(engagementString)
    }
}
