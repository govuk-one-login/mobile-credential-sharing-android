package uk.gov.onelogin.sharing.verifier.scan

import android.net.Uri

sealed class BarcodeDataResult {
    data object NotFound : BarcodeDataResult()
    data class Found(val data: Uri) : BarcodeDataResult()
}
