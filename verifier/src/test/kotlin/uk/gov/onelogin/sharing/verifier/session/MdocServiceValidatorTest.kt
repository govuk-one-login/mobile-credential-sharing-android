package uk.gov.onelogin.sharing.verifier.session

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.core.mdoc.GattUuids

class MdocServiceValidatorTest {

    private val fakeLogger = SystemLogger()
    private val validator = MdocServiceValidator(
        logger = fakeLogger
    )

    @Test
    fun `validate returns Success when all required characteristics present`() {
        val service = mockk<BluetoothGattService>()

        val stateChar = mockk<BluetoothGattCharacteristic>()
        val c2sChar = mockk<BluetoothGattCharacteristic>()
        val s2cChar = mockk<BluetoothGattCharacteristic>()

        every { stateChar.properties } returns (
            BluetoothGattCharacteristic.PROPERTY_NOTIFY or
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
            )
        every { c2sChar.properties } returns BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
        every { s2cChar.properties } returns BluetoothGattCharacteristic.PROPERTY_NOTIFY

        every { service.getCharacteristic(GattUuids.STATE_UUID) } returns stateChar
        every { service.getCharacteristic(GattUuids.CLIENT_2_SERVER_UUID) } returns c2sChar
        every { service.getCharacteristic(GattUuids.SERVER_2_CLIENT_UUID) } returns s2cChar

        val result = validator.validate(service)

        assertEquals(ValidationResult.Success, result)
        assertTrue(fakeLogger.size == 0)
    }

    @Test
    fun `validate returns Failure and logs error when characteristics are missing`() {
        val service = mockk<BluetoothGattService>()

        val stateChar = mockk<BluetoothGattCharacteristic>()
        every { service.getCharacteristic(GattUuids.STATE_UUID) } returns stateChar
        every { service.getCharacteristic(GattUuids.CLIENT_2_SERVER_UUID) } returns null
        every { service.getCharacteristic(GattUuids.SERVER_2_CLIENT_UUID) } returns null

        every { stateChar.properties } returns (
            BluetoothGattCharacteristic.PROPERTY_NOTIFY or
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
            )

        val result = validator.validate(service)

        require(result is ValidationResult.Failure)
        assertEquals(2, result.errors.size)
        assertTrue(
            result.errors.contains(
                "Client2Server characteristic not found (${GattUuids.CLIENT_2_SERVER_UUID})"
            )
        )
        assertTrue(
            result.errors.contains(
                "Server2Client characteristic not found (${GattUuids.SERVER_2_CLIENT_UUID})"
            )
        )

        assertEquals(2, fakeLogger.size)
        assertTrue(fakeLogger.contains("Missing required Client2Server characteristic"))
        assertTrue(fakeLogger.contains("Missing required Server2Client characteristic"))
    }

    @Test
    fun `validate returns Failure and logs error when properties don't match`() {
        val service = mockk<BluetoothGattService>()

        val stateChar = mockk<BluetoothGattCharacteristic>()
        val c2sChar = mockk<BluetoothGattCharacteristic>()
        val s2cChar = mockk<BluetoothGattCharacteristic>()

        every { stateChar.properties } returns (
            BluetoothGattCharacteristic.PROPERTY_NOTIFY or
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
            )
        every { c2sChar.properties } returns BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
        // wrong property
        every { s2cChar.properties } returns BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE

        every { service.getCharacteristic(GattUuids.STATE_UUID) } returns stateChar
        every { service.getCharacteristic(GattUuids.CLIENT_2_SERVER_UUID) } returns c2sChar
        every { service.getCharacteristic(GattUuids.SERVER_2_CLIENT_UUID) } returns s2cChar

        val result = validator.validate(service)
        require(result is ValidationResult.Failure)

        assertTrue(
            result.errors.contains(
                "Server2Client characteristic missing property:" +
                    " ${BluetoothGattCharacteristic.PROPERTY_NOTIFY}"
            )
        )

        assertEquals(1, fakeLogger.size)
    }
}
