package uk.gov.onelogin.sharing.bluetooth.internal.client

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.internal.mdoc.GattUuids
import uk.gov.onelogin.sharing.core.logger.logTag
import java.util.UUID

internal class MdocServiceValidator(
    private val serviceUuids: GattUuids,
    private val logger: Logger,
): ServiceValidator {
    override fun validate(service: BluetoothGattService): ValidationResult {
        val errors = mutableListOf<String>()

        validateCharacteristic(
            service,
            serviceUuids.STATE_UUID,
            "State",
            errors
        )

        validateCharacteristic(
            service,
            serviceUuids.CLIENT_2_SERVER_UUID,
            "Client2Server",
            errors
        )

        validateCharacteristic(
            service,
            serviceUuids.SERVER_2_CLIENT_UUID,
            "Server2Client",
            errors
        )

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(errors)
        }
    }

    private fun validateCharacteristic(
        service: BluetoothGattService,
        uuid: UUID,
        name: String,
        errors: MutableList<String>
    ): BluetoothGattCharacteristic? {
        val characteristic = service.getCharacteristic(uuid)
        if (characteristic == null) {
            logger.error(logTag, "Missing required $name characteristic")
            errors.add("$name characteristic not found ($uuid)")
            return null
        }
        return characteristic
    }
}