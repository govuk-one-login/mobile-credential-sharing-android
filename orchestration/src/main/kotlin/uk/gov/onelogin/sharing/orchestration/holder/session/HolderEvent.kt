package uk.gov.onelogin.sharing.orchestration.holder.session

sealed interface HolderEvent {
    data class QrCodeReady(val data: String) : HolderEvent
}
