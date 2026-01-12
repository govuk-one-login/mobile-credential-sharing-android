package uk.gov.onelogin.sharing.bluetooth.internal.validator

import android.bluetooth.BluetoothGattService
import uk.gov.onelogin.sharing.bluetooth.internal.validator.ServiceValidator
import uk.gov.onelogin.sharing.bluetooth.internal.validator.ValidationResult

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
