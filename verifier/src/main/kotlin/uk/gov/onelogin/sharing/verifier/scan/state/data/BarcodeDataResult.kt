package uk.gov.onelogin.sharing.verifier.scan.state.data

/**
 * Sealed class representing a state table for the
 * [uk.gov.onelogin.sharing.verifier.scan.callbacks.VerifierScannerBarcodeScanCallback].
 */
sealed class BarcodeDataResult {
    /**
     * State for when a QR code cannot be found.
     */
    data object NotFound : BarcodeDataResult()

    /**
     * State for when finding a QR code containing an `mdoc:` URI.
     */
    data class Valid(val data: String) : BarcodeDataResult()

    /**
     * State for when finding a QR code containing a URI with an invalid scheme, such as `https:`.
     */
    data class Invalid(val data: String) : BarcodeDataResult()
}
