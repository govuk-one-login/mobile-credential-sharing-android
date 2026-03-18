package uk.gov.onelogin.sharing.security.engagement

import org.junit.Assert.assertNotNull
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.engagement.EngagementGeneratorStub.qrCodeEngagementUuid

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
