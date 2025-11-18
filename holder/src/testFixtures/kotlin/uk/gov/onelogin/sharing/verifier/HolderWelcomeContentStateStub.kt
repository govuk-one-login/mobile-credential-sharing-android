package uk.gov.onelogin.sharing.verifier

import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.api.AdvertiserState
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeUiState

fun stubHolderWelcomeUiState(
    errorMessage: String? = null,
    advertiserState: AdvertiserState = AdvertiserState.Idle,
    uuid: UUID = UUID.randomUUID(),
    qrCodeData: String? = "some-qr-data"
) = HolderWelcomeUiState(
    lastErrorMessage = errorMessage,
    advertiserState = advertiserState,
    uuid = uuid,
    qrData = qrCodeData
)
