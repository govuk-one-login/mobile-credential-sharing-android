package uk.gov.onelogin.sharing.cryptoService.scanner

import uk.gov.onelogin.sharing.core.SharingSessionScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding

@ContributesBinding(SharingSessionScope::class, binding = binding<QrParser>())
class QrParserImpl : QrParser {
    companion object {
        const val QR_CODE_SCHEME = "mdoc:"
    }

    override fun parse(rawBarcode: String?): QrScanResult {
        if (rawBarcode.isNullOrBlank()) return QrScanResult.NotFound

        return if (rawBarcode.startsWith(QR_CODE_SCHEME)) {
            QrScanResult.Success(rawBarcode.removePrefix(QR_CODE_SCHEME))
        } else {
            QrScanResult.Invalid(rawBarcode)
        }
    }
}
