package uk.gov.onelogin.sharing.bluetooth.internal.client

import android.bluetooth.BluetoothGattService

internal interface ServiceValidator {

    fun validate(service: BluetoothGattService): ValidationResult
}

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Failure(val errors: List<String>) : ValidationResult()
}