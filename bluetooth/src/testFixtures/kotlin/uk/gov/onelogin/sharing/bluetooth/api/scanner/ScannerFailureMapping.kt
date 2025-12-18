package uk.gov.onelogin.sharing.bluetooth.api.scanner

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback

enum class ScannerFailureMapping(val errorCode: Int, val expectedFailure: ScannerFailure) {
    ALREADY_STARTED_SCANNING(
        errorCode = ScanCallback.SCAN_FAILED_ALREADY_STARTED,
        expectedFailure = ScannerFailure.ALREADY_STARTED_SCANNING
    ),
    INTERNAL_ERROR(
        errorCode = ScanCallback.SCAN_FAILED_INTERNAL_ERROR,
        expectedFailure = ScannerFailure.INTERNAL_ERROR
    ),

    @SuppressLint("InlinedApi")
    OUT_OF_RESOURCES(
        errorCode = ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES,
        expectedFailure = ScannerFailure.OUT_OF_RESOURCES
    ),
    REGISTRATION_FAILED(
        errorCode = ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED,
        expectedFailure = ScannerFailure.REGISTRATION_FAILED
    ),

    @SuppressLint("InlinedApi")
    SCANNING_TOO_FREQUENTLY(
        errorCode = ScanCallback.SCAN_FAILED_SCANNING_TOO_FREQUENTLY,
        expectedFailure = ScannerFailure.SCANNING_TOO_FREQUENTLY
    ),
    UNKNOWN(
        errorCode = -1,
        expectedFailure = ScannerFailure.UNKNOWN
    ),
    UNSUPPORTED_FEATURE(
        errorCode = ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED,
        expectedFailure = ScannerFailure.UNSUPPORTED_FEATURE
    )
}
