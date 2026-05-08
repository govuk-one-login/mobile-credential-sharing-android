package uk.gov.onelogin.sharing.orchestration.verifier.credential

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig
import uk.gov.onelogin.sharing.orchestration.verifier.session.BuildDeviceRequestUseCase

@Inject
@ContributesBinding(AppScope::class)
class DeviceRequestHandlerImpl(
    private val verifierConfig: VerifierConfig,
    private val buildDeviceRequestUseCase: BuildDeviceRequestUseCase
) : DeviceRequestHandler {

    override fun buildAndEncrypt(skReader: ByteArray, encryptCounter: UInt): ByteArray =
        buildDeviceRequestUseCase.execute(
            verificationRequest = verifierConfig.verificationRequest,
            skReader = skReader,
            encryptCounter = encryptCounter
        )
}
