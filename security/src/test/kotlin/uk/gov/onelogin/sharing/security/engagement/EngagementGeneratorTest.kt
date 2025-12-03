package uk.gov.onelogin.sharing.security.engagement

import org.junit.Assert.assertNotNull
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.engagement.EngagementGeneratorStub.qrCodeEngagementUuid

class EngagementGeneratorTest {

    private val engagementGenerator: EngagementGenerator = EngagementGenerator(
        logger = SystemLogger()
    )

    @Test
    fun `generates base 64 encoded string for device engagement`() {
        val key = SessionSecurityTestStub.generateValidKeyPair()
        val coseKey = CoseKey.generateCoseKey(key!!)

        val engagementString = engagementGenerator.qrCodeEngagement(coseKey, qrCodeEngagementUuid)
        assertNotNull(engagementString)
    }
}
