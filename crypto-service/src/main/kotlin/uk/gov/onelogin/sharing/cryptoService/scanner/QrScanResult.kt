package uk.gov.onelogin.sharing.cryptoService.scanner

sealed interface QrScanResult {
    data class Success(val value: String) : QrScanResult
    data class Invalid(val rawValue: String) : QrScanResult
    data object NotFound : QrScanResult
}
