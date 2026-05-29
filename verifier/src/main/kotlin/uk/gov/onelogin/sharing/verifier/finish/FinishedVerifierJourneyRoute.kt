package uk.gov.onelogin.sharing.verifier.finish

import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse

@Keep
@Serializable
internal data class FinishedVerifierJourneyRoute(val response: DeviceResponse)
