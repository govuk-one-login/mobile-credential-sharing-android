package uk.gov.onelogin.sharing.verification

import org.junit.Test
import uk.gov.onelogin.sharing.verification.ClassInfoExt.assertInterfaceSignatures
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult

class DeviceSignedTest {

    /**
     * DCMAW-20245: AC7: [DeviceSigned] exposes `deviceNameSpacesBytes: ByteArray` and
     * `deviceSignature: ByteArray` as defined.
     */
    @Test
    fun `Ensure VerifiableDocument constraints`() {
        val expectedMethods = listOf(
            "getDeviceNameSpacesBytes" to ByteArray::class.java,
            "getDeviceSignature" to ByteArray::class.java
        )

        val classInfo = scanResult.getClassInfo(DeviceSigned::class.java.name)

        assertInterfaceSignatures(expectedMethods, classInfo)
    }
}