package uk.gov.onelogin.sharing.verifier

import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import org.hamcrest.CoreMatchers.instanceOf
import uk.gov.onelogin.sharing.cryptoService.verifier.DeviceResponseStub
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionStateStubs.preflightEmptyPermissions
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.connect.error.BluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.verifier.error.UnrecoverableVerifierErrorRoute
import uk.gov.onelogin.sharing.verifier.finish.FinishedVerifierJourneyRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.verify.VerifierPrerequisitesRoute
import uk.gov.onelogin.sharing.verifier.verify.retry.RetryVerifierPrerequisitesRoute

private typealias InputParameters = Triple<
    String,
    VerifierSessionState,
    TestNavHostController.() -> Boolean
    >

/**
 * Parameterised test inputs for verifying navigation endpoints based on [VerifierSessionState].
 *
 * Due to the routes being internal to the module, verification occurs in the proceeding way:
 *
 * ```kotlin
 * // Returns a `String?`
 * controller.currentBackStackEntry?.destination?.route
 * ```
 */
class VerifierStateToNavigationRoute : TestParametersValuesProvider() {
    private val inputs =
        listOf<InputParameters>(
            Triple(
                "'NotStarted' -> VerifierPrerequisitesRoute",
                VerifierSessionState.NotStarted
            ) {
                instanceOf<VerifierPrerequisitesRoute>(
                    VerifierPrerequisitesRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<VerifierPrerequisitesRoute>()
                )
            },
            Triple(
                "'Preflight' -> RetryVerifierPrerequisitesRoute",
                preflightEmptyPermissions
            ) {
                instanceOf<RetryVerifierPrerequisitesRoute>(
                    RetryVerifierPrerequisitesRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<RetryVerifierPrerequisitesRoute>()
                )
            },
            Triple(
                "'ReadyToScan' -> VerifierScanRoute",
                VerifierSessionState.ReadyToScan
            ) {
                instanceOf<VerifierScanRoute>(
                    VerifierScanRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<VerifierScanRoute>()
                )
            },
            Triple(
                "'Connecting' -> ConnectWithHolderDeviceRoute",
                VerifierSessionState.Connecting
            ) {
                instanceOf<ConnectWithHolderDeviceRoute>(
                    ConnectWithHolderDeviceRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<ConnectWithHolderDeviceRoute>()
                )
            },
            Triple(
                "Failure: UnsupportedQrCodeFormat -> BluetoothConnectionErrorRoute",
                VerifierSessionState.Complete.Failed(
                    SessionError(
                        "This is a unit test",
                        SessionErrorReason.UnsupportedQrCodeFormat("")
                    )
                )
            ) {
                instanceOf<ScannedInvalidQrRoute>(
                    ScannedInvalidQrRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<ScannedInvalidQrRoute>()
                )
            },
            Triple(
                "Failure: ServiceUuidNotFound -> BluetoothConnectionErrorRoute",
                VerifierSessionState.Complete.Failed(
                    SessionError(
                        "This is a unit test",
                        SessionErrorReason.ServiceUuidNotFound
                    )
                )
            ) {
                instanceOf<BluetoothConnectionErrorRoute>(
                    BluetoothConnectionErrorRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<BluetoothConnectionErrorRoute>()
                )
            },
            Triple(
                "Failure: generic -> UnrecoverableVerifierErrorRoute",
                VerifierSessionState.Complete.Failed(
                    SessionError(
                        "This is a unit test",
                        SessionErrorReason.UnrecoverableThrowable(Exception())
                    )
                )
            ) {
                instanceOf<UnrecoverableVerifierErrorRoute>(
                    UnrecoverableVerifierErrorRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<UnrecoverableVerifierErrorRoute>()
                )
            },
            Triple(
                "Failure: DeviceRequestProcessingError -> UnrecoverableVerifierErrorRoute",
                VerifierSessionState.Complete.Failed(
                    SessionError(
                        "DeviceRequest processing error",
                        SessionErrorReason.DeviceRequestProcessingError(10u)
                    )
                )
            ) {
                instanceOf<UnrecoverableVerifierErrorRoute>(
                    UnrecoverableVerifierErrorRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<UnrecoverableVerifierErrorRoute>()
                )
            },
            Triple(
                "Failure: DocumentNotReturned -> UnrecoverableVerifierErrorRoute",
                VerifierSessionState.Complete.Failed(
                    SessionError(
                        "Document not returned",
                        SessionErrorReason.DocumentNotReturned
                    )
                )
            ) {
                instanceOf<UnrecoverableVerifierErrorRoute>(
                    UnrecoverableVerifierErrorRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<UnrecoverableVerifierErrorRoute>()
                )
            },
            Triple(
                "Successfully finishes the verifier journey",
                VerifierSessionState.Complete.Success(
                    DeviceResponseStub.successWithDocuments
                )
            ) {
                instanceOf<FinishedVerifierJourneyRoute>(
                    FinishedVerifierJourneyRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<FinishedVerifierJourneyRoute>()
                )
            }
        )

    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues?> =
        inputs.map { (name, state, assertion) ->
            TestParameters.TestParametersValues.builder()
                .name(name)
                .addParameter("state", state)
                .addParameter("assertion", assertion)
                .build()
        }
}
