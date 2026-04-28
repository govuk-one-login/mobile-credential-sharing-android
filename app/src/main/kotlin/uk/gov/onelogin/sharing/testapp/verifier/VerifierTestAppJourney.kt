package uk.gov.onelogin.sharing.testapp.verifier

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest

@Parcelize
@Serializable
internal data class VerifierTestAppJourney(val request: VerificationRequest) : Parcelable
