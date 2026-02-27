package uk.gov.onelogin.sharing.bluetooth.internal.validator

import android.bluetooth.BluetoothGattService

class FakeServiceValidator : ServiceValidator {
    var errors = mutableListOf<String>()
    var calls = 0

    override fun validate(service: BluetoothGattService): ValidationResult {
        calls++

        return if (errors.isNotEmpty()) {
            ValidationResult.Failure(errors)
        } else {
            ValidationResult.Success
        }
    }
}
