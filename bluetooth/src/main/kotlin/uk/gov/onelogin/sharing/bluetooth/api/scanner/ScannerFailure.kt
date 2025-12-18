package uk.gov.onelogin.sharing.bluetooth.api.scanner

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback

enum class ScannerFailure(private val internalErrorCode: Int) {
    /**
     * @see ScanCallback.SCAN_FAILED_ALREADY_STARTED
     */
    ALREADY_STARTED_SCANNING(
        internalErrorCode = ScanCallback.SCAN_FAILED_ALREADY_STARTED
    ),

    /**
     * @see ScanCallback.SCAN_FAILED_INTERNAL_ERROR
     */
    INTERNAL_ERROR(
        internalErrorCode = ScanCallback.SCAN_FAILED_INTERNAL_ERROR
    ),

    /**
     * @see ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES
     */
    @SuppressLint("InlinedApi")
    OUT_OF_RESOURCES(
        internalErrorCode = ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES
    ),

    /**
     * @see ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED
     */
    REGISTRATION_FAILED(
        internalErrorCode = ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED
    ),

    /**
     * @see ScanCallback.SCAN_FAILED_SCANNING_TOO_FREQUENTLY
     */
    @SuppressLint("InlinedApi")
    SCANNING_TOO_FREQUENTLY(
        internalErrorCode = ScanCallback.SCAN_FAILED_SCANNING_TOO_FREQUENTLY
    ),

    /**
     * Null-value failure state when the reason cannot be identified.
     */
    UNKNOWN(internalErrorCode = -1),

    /**
     * @see ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED
     */
    UNSUPPORTED_FEATURE(
        internalErrorCode = ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED
    );

    companion object {
        @JvmStatic
        fun from(errorCode: Int) = entries.firstOrNull {
            it.internalErrorCode == errorCode
        } ?: UNKNOWN
    }
}
