package uk.gov.onelogin.sharing.cryptoService.scanner

interface QrParser {
    fun parse(rawBarcode: String?): QrScanResult
}
