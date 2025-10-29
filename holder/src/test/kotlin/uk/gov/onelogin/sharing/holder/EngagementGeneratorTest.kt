package uk.gov.onelogin.sharing.holder

import junit.framework.TestCase.assertNotNull
import org.junit.Test
import uk.gov.onelogin.sharing.holder.engagement.EngagementGenerator

class EngagementGeneratorTest {

    private val engagementGenerator: EngagementGenerator = EngagementGenerator()

    @Test
    fun `generates base 64 encoded string for device engagement`() {
        val engagementString = engagementGenerator.generateEncodedBase64QrEngagement()
        assertNotNull(engagementString)
    }
}
