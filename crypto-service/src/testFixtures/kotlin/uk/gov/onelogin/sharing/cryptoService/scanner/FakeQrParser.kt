package uk.gov.onelogin.sharing.cryptoService.scanner

import uk.gov.onelogin.sharing.cryptoService.scanner.QrParserImpl.Companion.QR_CODE_SCHEME

class FakeQrParser : QrParser {

    override fun parse(rawBarcode: String?): QrScanResult {
        if (rawBarcode.isNullOrBlank()) return QrScanResult.NotFound

        return if (rawBarcode.startsWith(QR_CODE_SCHEME)) {
            QrScanResult.Success(rawBarcode.removePrefix(QR_CODE_SCHEME))
        } else {
            QrScanResult.Invalid(rawBarcode)
        }
    }
}
