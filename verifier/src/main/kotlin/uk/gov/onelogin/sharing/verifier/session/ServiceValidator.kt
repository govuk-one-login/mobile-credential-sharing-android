package uk.gov.onelogin.sharing.verifier.session

import android.bluetooth.BluetoothGattService

/**
 * Validates a particular service [BluetoothGattService].
 */
fun interface ServiceValidator {
    /**
     * Validates the given [BluetoothGattService].
     *
     * @param service The service to validate.
     * @return A [ValidationResult] indicating success or failure.
     */
    fun validate(service: BluetoothGattService): ValidationResult
}

/**
 * Represents the result of a service validation.
 */
sealed class ValidationResult {
    /**
     * Indicates that the service characteristics are valid.
     */
    data object Success : ValidationResult()

    /**
     * Service characteristics are invalid.
     *
     * @param errors A list of error messages describing why validation failed.
     */
    data class Failure(val errors: List<String>) : ValidationResult()
}
