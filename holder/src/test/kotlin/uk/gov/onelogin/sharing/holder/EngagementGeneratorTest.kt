package uk.gov.onelogin.sharing.holder

import junit.framework.TestCase.assertNotNull
import org.junit.Test
import uk.gov.onelogin.sharing.holder.engagement.EngagementGenerator
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateValidKeyPair
import uk.gov.onelogin.sharing.security.cose.CoseKey

class EngagementGeneratorTest {

    private val engagementGenerator: EngagementGenerator = EngagementGenerator()

    @Test
    fun `generates base 64 encoded string for device engagement`() {
        val key = generateValidKeyPair()
        val coseKey = CoseKey.generateCoseKey(key!!)

        val engagementString = engagementGenerator.qrCodeEngagement(coseKey)
        assertNotNull(engagementString)
    }
}
