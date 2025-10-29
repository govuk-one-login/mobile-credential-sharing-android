package uk.gov.onelogin.sharing.verifier

import uk.gov.onelogin.sharing.holder.engagement.Engagement

/**
 * Test fake to be used by other classes if required
 *
 * */

class FakeEngagementGenerator : Engagement {
    override fun generateEncodedBase64QrEngagement(): String = BASE64_ENCODED_DEVICE_ENGAGEMENT

    companion object {
        const val BASE64_ENCODED_DEVICE_ENGAGEMENT =
            "v2EwYzEuMGExnwHYGFBGQUtFX0VERVZJQ0VfS0VZ/2Eyn58CAb9hMPVhMfRiMTDYGFgkMTExMTExMTEtMjIyMi0zMzMzLTQ0NDQtNTU1NTU1NTU1NTU1/////w=="
    }
}
