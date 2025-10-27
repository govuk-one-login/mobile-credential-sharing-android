package uk.gov.onelogin.sharing.models

import uk.gov.onelogin.sharing.models.MdocStubStrings.UUID
import uk.gov.onelogin.sharing.models.SecurityTestStub.SECURITY
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement

/**
 * Dummy object to provide some content within the test fixtures source set.
 *
 */
object DeviceEngagementStub {
    private fun deviceEngagementBuilder(): DeviceEngagement.Builder =
        DeviceEngagement.builder(SECURITY)
            .version("1.0")
            .ble(peripheralUuid = UUID)

    val DEVICE_ENGAGEMENT: DeviceEngagement = deviceEngagementBuilder().build()
}
