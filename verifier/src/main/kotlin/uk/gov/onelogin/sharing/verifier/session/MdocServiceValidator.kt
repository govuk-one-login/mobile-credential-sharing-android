package uk.gov.onelogin.sharing.verifier.session

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import java.util.UUID
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.core.mdoc.GattUuids

/**
 * Checks for the presence of mandatory characteristics defined in [GattUuids]
 * 11.1.3.2 Service definition - ISO 18013-5
 *
 * @param serviceUuids The collection of UUIDs defining the expected service and characteristics.
 * @param logger An instance of [Logger] for logging validation errors.
 */
class MdocServiceValidator(private val serviceUuids: GattUuids, private val logger: Logger) :
    ServiceValidator {
    override fun validate(service: BluetoothGattService): ValidationResult {
        val errors = mutableListOf<String>()

        validateCharacteristic(
            service = service,
            uuid = serviceUuids.STATE_UUID,
            name = "State",
            requiredProperties = listOf(
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
            ),
            errors = errors
        )

        validateCharacteristic(
            service = service,
            uuid = serviceUuids.CLIENT_2_SERVER_UUID,
            name = "Client2Server",
            requiredProperties = listOf(
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
            ),
            errors = errors
        )

        validateCharacteristic(
            service = service,
            uuid = serviceUuids.SERVER_2_CLIENT_UUID,
            name = "Server2Client",
            requiredProperties = listOf(
                BluetoothGattCharacteristic.PROPERTY_NOTIFY
            ),
            errors = errors
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
        requiredProperties: List<Int>,
        errors: MutableList<String>
    ): BluetoothGattCharacteristic? {
        val characteristic = service.getCharacteristic(uuid)
        if (characteristic == null) {
            logger.error(logTag, "Missing required $name characteristic")
            errors.add("$name characteristic not found ($uuid)")
            return null
        }

        requiredProperties.forEach { property ->
            if (!characteristic.hasProperty(property)) {
                logger.error(
                    logTag,
                    "$name characteristic missing required property: $property"
                )
                errors.add("$name characteristic missing property: $property")
            }
        }

        return characteristic
    }

    private fun BluetoothGattCharacteristic.hasProperty(property: Int): Boolean =
        properties and property == property
}
