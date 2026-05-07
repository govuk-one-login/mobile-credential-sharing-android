package uk.gov.onelogin.sharing.orchestration.verifier.session

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeCbor
import uk.gov.onelogin.sharing.cryptoService.verifier.EncryptDeviceRequestUseCase
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest
import uk.gov.onelogin.sharing.orchestration.verificationrequest.toItemsRequest

@Inject
@ContributesBinding(AppScope::class, binding = binding<BuildDeviceRequestUseCase>())
class BuildDeviceRequestUseCaseImpl(
    private val encryptDeviceRequestUseCase: EncryptDeviceRequestUseCase,
    private val logger: Logger
) : BuildDeviceRequestUseCase {

    override fun execute(
        verificationRequest: VerificationRequest,
        skReader: ByteArray,
        encryptCounter: UInt
    ): ByteArray {
        val itemsRequest = verificationRequest.attributeGroup
            .toItemsRequest(verificationRequest.documentType)
        logger.debug(logTag, "ItemsRequest: $itemsRequest")

        val deviceRequestBytes = DeviceRequest(
            version = "1.0",
            docRequests = listOf(DocRequest(itemsRequest))
        ).encodeCbor()
        logger.debug(logTag, "DeviceRequest bytes: ${deviceRequestBytes.toHexString()}")

        return encryptDeviceRequestUseCase.encrypt(
            deviceRequestBytes = deviceRequestBytes,
            skReader = skReader,
            encryptCounter = encryptCounter
        )
    }
}
