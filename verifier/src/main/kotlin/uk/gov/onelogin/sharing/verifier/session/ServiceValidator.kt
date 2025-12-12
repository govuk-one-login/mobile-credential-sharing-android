package uk.gov.onelogin.sharing.verifier.session

import android.bluetooth.BluetoothGattService

fun interface ServiceValidator {
    fun validate(service: BluetoothGattService): ValidationResult
}

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Failure(val errors: List<String>) : ValidationResult()
}
