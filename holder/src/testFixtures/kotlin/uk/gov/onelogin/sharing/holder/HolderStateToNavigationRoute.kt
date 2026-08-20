package uk.gov.onelogin.sharing.holder

import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BtConnectionErrorRoute
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub
import uk.gov.onelogin.sharing.holder.awaitingresolution.AwaitingVerifierResolutionRoute
import uk.gov.onelogin.sharing.holder.cancellation.HolderCancellationScreenRoute
import uk.gov.onelogin.sharing.holder.consent.HolderConsentRoute
import uk.gov.onelogin.sharing.holder.error.UnrecoverableHolderErrorRoute
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesRoute
import uk.gov.onelogin.sharing.holder.prerequisites.retry.RetryHolderPrerequisitesRoute
import uk.gov.onelogin.sharing.holder.presentation.HolderPresentQrRoute
import uk.gov.onelogin.sharing.holder.success.HolderSuccessRoute
import uk.gov.onelogin.sharing.orchestration.exceptions.BluetoothDisconnectedException
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs
import uk.gov.onelogin.sharing.orchestration.session.SessionError

private typealias InputParameters = Triple<
    String,
    HolderSessionState,
    TestNavHostController.() -> Boolean
    >

/**
 * Parameterised test inputs for verifying navigation endpoints based on [HolderSessionState].
 *
 * Due to the routes being internal to the module, verification occurs in the proceeding way:
 *
 * ```kotlin
 * // Returns a `String?`
 * controller.currentBackStackEntry?.destination?.route
 * ```
 */
class HolderStateToNavigationRoute : TestParametersValuesProvider() {
    private val inputs =
        listOf<InputParameters>(
            Triple(
                "'NotStarted' -> HolderPrerequisitesRoute",
                HolderSessionState.NotStarted
            ) {
                instanceOf<HolderPrerequisitesRoute>(
                    HolderPrerequisitesRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<HolderPrerequisitesRoute>()
                )
            },
            Triple(
                "'Preflight' -> RetryHolderPrerequisitesRoute",
                HolderSessionStateStubs.preflightEmptyPermissions
            ) {
                instanceOf<RetryHolderPrerequisitesRoute>(
                    RetryHolderPrerequisitesRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<RetryHolderPrerequisitesRoute>()
                )
            },
            Triple(
                "'AwaitingUserConsent' -> AwaitingUserConsent",
                HolderSessionState.AwaitingUserConsent(DeviceRequestStub.deviceRequestStub)
            ) {
                instanceOf<HolderConsentRoute>(
                    HolderConsentRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<HolderConsentRoute>()
                )
            },
            Triple(
                "'PresentingEngagement' -> HolderPresentQrRoute",
                HolderSessionState.PresentingEngagement("")
            ) {
                instanceOf<HolderPresentQrRoute>(
                    HolderPresentQrRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<HolderPresentQrRoute>()
                )
            },
            Triple(
                "'AwaitingVerifierResolution' -> AwaitingVerifierResolutionRoute",
                HolderSessionState.AwaitingVerifierResolution
            ) {
                instanceOf<AwaitingVerifierResolutionRoute>(
                    AwaitingVerifierResolutionRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<AwaitingVerifierResolutionRoute>()
                )
            },
            Triple(
                "Failure: Bluetooth connection error -> BtConnectionErrorRoute",
                HolderSessionState.Complete.Failed(
                    SessionError(
                        "This is a unit test",
                        BluetoothDisconnectedException(
                            "This is a bluetooth exception",
                            Exception()
                        )
                    )
                )
            ) {
                instanceOf<BtConnectionErrorRoute>(
                    BtConnectionErrorRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<BtConnectionErrorRoute>()
                )
            },
            Triple(
                "Failure: Generic -> Unrecoverable error",
                HolderSessionState.Complete.Failed(
                    SessionError(
                        "",
                        Exception()
                    )
                )
            ) {
                instanceOf<UnrecoverableHolderErrorRoute>(
                    UnrecoverableHolderErrorRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<UnrecoverableHolderErrorRoute>()
                )
            },
            Triple(
                "'Complete.Success(UnfulfillableRequest)' -> HolderSuccessRoute",
                HolderSessionState.Complete.Success(
                    HolderSessionState.Complete.SuccessReason.UnfulfillableRequest
                )
            ) {
                instanceOf<HolderSuccessRoute>(
                    HolderSuccessRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<HolderSuccessRoute>()
                )
            },
            Triple(
                "'Complete.Success(Denied)' -> HolderSuccessRoute(immediatelyReset = true)",
                HolderSessionState.Complete.Success(
                    HolderSessionState.Complete.SuccessReason.Denied
                )
            ) {
                equalTo(HolderSuccessRoute(immediatelyReset = true)).matches(
                    currentBackStackEntry?.toRoute<HolderSuccessRoute>()
                )
            },
            Triple(
                "'Complete.Cancelled' -> HolderCancellationScreenRoute",
                HolderSessionState.Complete.Cancelled
            ) {
                instanceOf<HolderCancellationScreenRoute>(
                    HolderCancellationScreenRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<HolderCancellationScreenRoute>()
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
