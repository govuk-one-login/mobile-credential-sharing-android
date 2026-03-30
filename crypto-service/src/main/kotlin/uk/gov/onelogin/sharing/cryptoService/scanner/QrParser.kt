package uk.gov.onelogin.sharing.cryptoService.scanner

fun interface QrParser {
    fun parse(rawBarcode: String?): QrScanResult
}
