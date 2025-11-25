package uk.gov.onelogin.sharing.holder

import java.util.UUID
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateValidKeyPair
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.engagement.EngagementGenerator

class EngagementGeneratorTest {

    private val engagementGenerator: EngagementGenerator = EngagementGenerator()

    @Test
    fun `generates base 64 encoded string for device engagement`() {
        val key = generateValidKeyPair()
        val coseKey = CoseKey.generateCoseKey(key!!)
        val uuid = UUID.randomUUID()

        val engagementString = engagementGenerator.qrCodeEngagement(coseKey, uuid)
        assertNotNull(engagementString)
    }
}
