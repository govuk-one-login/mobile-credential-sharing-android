package uk.gov.onelogin.sharing.verifier.finish

import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.orchestration.session.DeviceResponse

@Keep
@Serializable
internal data class FinishedVerifierJourneyRoute(
    val response: DeviceResponse
)
{}