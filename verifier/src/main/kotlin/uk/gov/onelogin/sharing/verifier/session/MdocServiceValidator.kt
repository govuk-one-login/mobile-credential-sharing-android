package uk.gov.onelogin.sharing.verifier.session

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import java.util.UUID
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.core.mdoc.GattUuids

/**
 * Checks for the presence of mandatory characteristics defined in [GattUuids]
 * 11.1.3.2 Service definition - ISO 18013-5
 *
 * @param logger An instance of [Logger] for logging validation errors.
 */
@Inject
@ContributesBinding(ViewModelScope::class)
class MdocServiceValidator(private val logger: Logger) : ServiceValidator {
    override fun validate(service: BluetoothGattService): ValidationResult {
        val errors = mutableListOf<String>()

        validateCharacteristic(
            service = service,
            uuid = GattUuids.STATE_UUID,
            name = "State",
            requiredProperties = listOf(
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
            ),
            errors = errors
        )

        validateCharacteristic(
            service = service,
            uuid = GattUuids.CLIENT_2_SERVER_UUID,
            name = "Client2Server",
            requiredProperties = listOf(
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
            ),
            errors = errors
        )

        validateCharacteristic(
            service = service,
            uuid = GattUuids.SERVER_2_CLIENT_UUID,
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
