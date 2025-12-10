package uk.gov.onelogin.sharing.bluetooth.client

import android.bluetooth.BluetoothGattService
import uk.gov.onelogin.sharing.bluetooth.internal.client.ServiceValidator
import uk.gov.onelogin.sharing.bluetooth.internal.client.ValidationResult

class FakeServiceValidator : ServiceValidator {
    val errors = mutableListOf<String>()
    override fun validate(service: BluetoothGattService): ValidationResult {
        if (errors.isNotEmpty()) {
            return ValidationResult.Failure(errors)
        } else {
            return ValidationResult.Success
        }
    }
}