package uk.gov.onelogin.sharing.verifier

import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import org.hamcrest.CoreMatchers.instanceOf
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.connect.error.BluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.verifier.error.UnrecoverableVerifierErrorRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.verify.VerifierPrerequisitesRoute
import uk.gov.onelogin.sharing.verifier.verify.retry.RetryVerifierPrerequisitesRoute

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
class VerifierRouteParameters : TestParametersValuesProvider() {
    private val inputs =
        listOf<Triple<String, Any, TestNavHostController.() -> Boolean>>(
            Triple(
                "RetryVerifierPrerequisitesRoute",
                VerifierPrerequisitesRoute
            ) {
                instanceOf<VerifierPrerequisitesRoute>(
                    VerifierPrerequisitesRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<VerifierPrerequisitesRoute>()
                )
            },
            Triple(
                "RetryVerifierPrerequisitesRoute",
                RetryVerifierPrerequisitesRoute
            ) {
                instanceOf<RetryVerifierPrerequisitesRoute>(
                    RetryVerifierPrerequisitesRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<RetryVerifierPrerequisitesRoute>()
                )
            },
            Triple(
                "VerifierScanRoute",
                VerifierScanRoute
            ) {
                instanceOf<VerifierScanRoute>(
                    VerifierScanRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<VerifierScanRoute>()
                )
            },
            Triple(
                "ConnectWithHolderDeviceRoute",
                ConnectWithHolderDeviceRoute
            ) {
                instanceOf<ConnectWithHolderDeviceRoute>(
                    ConnectWithHolderDeviceRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<ConnectWithHolderDeviceRoute>()
                )
            },
            Triple(
                "ScannedInvalidQrRoute",
                ScannedInvalidQrRoute("")
            ) {
                instanceOf<ScannedInvalidQrRoute>(
                    ScannedInvalidQrRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<ScannedInvalidQrRoute>()
                )
            },
            Triple(
                "BluetoothConnectionErrorRoute",
                BluetoothConnectionErrorRoute("")
            ) {
                instanceOf<BluetoothConnectionErrorRoute>(
                    BluetoothConnectionErrorRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<BluetoothConnectionErrorRoute>()
                )
            },
            Triple(
                "UnrecoverableVerifierErrorRoute",
                UnrecoverableVerifierErrorRoute
            ) {
                instanceOf<UnrecoverableVerifierErrorRoute>(
                    UnrecoverableVerifierErrorRoute::class.java
                ).matches(
                    currentBackStackEntry?.toRoute<UnrecoverableVerifierErrorRoute>()
                )
            }
        )

    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues?> =
        inputs.map { (name, route, assertion) ->
            TestParameters.TestParametersValues.builder()
                .name(name)
                .addParameter("route", route)
                .addParameter("assertion", assertion)
                .build()
        }
}
