package uk.gov.onelogin.sharing.verifier.connect

import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail

@Keep
@Serializable
@ImplementationDetail(
    ticket = "DCMAW-16955",
    description = "Successful handling of scanned QR code"
)
internal data object ConnectWithHolderDeviceRoute
