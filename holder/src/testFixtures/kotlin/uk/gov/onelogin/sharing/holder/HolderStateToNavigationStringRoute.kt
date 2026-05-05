package uk.gov.onelogin.sharing.holder

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub
import uk.gov.onelogin.sharing.holder.consent.HolderConsentRoute
import uk.gov.onelogin.sharing.holder.error.UnrecoverableHolderErrorRoute
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesRoute
import uk.gov.onelogin.sharing.holder.prerequisites.retry.RetryHolderPrerequisitesRoute
import uk.gov.onelogin.sharing.holder.presentation.HolderPresentQrRoute
import uk.gov.onelogin.sharing.orchestration.exceptions.BluetoothDisconnectedException
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs
import uk.gov.onelogin.sharing.orchestration.session.SessionError

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
class HolderStateToNavigationStringRoute : TestParametersValuesProvider() {
    private val inputs = listOf(
        Triple(
            "'NotStarted' -> HolderPrerequisitesRoute",
            HolderSessionState.NotStarted,
            HolderPrerequisitesRoute.toString()
        ),
        Triple(
            "'Preflight' -> RetryHolderPrerequisitesRoute",
            HolderSessionStateStubs.preflightEmptyPermissions,
            RetryHolderPrerequisitesRoute.toString()
        ),
        Triple(
            "'AwaitingUserConsent' -> AwaitingUserConsent",
            HolderSessionState.AwaitingUserConsent(DeviceRequestStub.deviceRequestStub),
            HolderConsentRoute.toString()
        ),
        Triple(
            "'PresentingEngagement' -> HolderPresentQrRoute",
            HolderSessionState.PresentingEngagement(""),
            HolderPresentQrRoute.toString()
        ),
        Triple(
            "Failure: Bluetooth connection error -> BtConnectionErrorRoute",
            HolderSessionState.Complete.Failed(
                SessionError(
                    "",
                    BluetoothDisconnectedException("", Exception())
                )
            ),
            "BtConnectionErrorRoute/{title}"
        ),
        Triple(
            "Failure: Generic -> Unrecoverable error",
            HolderSessionState.Complete.Failed(
                SessionError(
                    "",
                    Exception()
                )
            ),
            UnrecoverableHolderErrorRoute.toString()
        )
    )

    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues?> =
        inputs.map { (name, state, expected) ->
            TestParameters.TestParametersValues.builder()
                .name(name)
                .addParameter("state", state)
                .addParameter("expectedRoute", expected)
                .build()
        }
}
