package uk.gov.onelogin.sharing.security.deviceretrievalmethods

import java.util.UUID
import org.junit.Assert
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.toByteArray

class BleExtensionsTest {

    @Test
    fun `toByteArray should return a 16-byte array`() {
        val uuid = UUID.randomUUID()

        val byteArray = uuid.toByteArray()

        Assert.assertEquals(16, byteArray.size)
    }
}
