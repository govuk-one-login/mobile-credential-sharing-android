package uk.gov.onelogin.sharing.holder

import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import org.hamcrest.CoreMatchers.instanceOf
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BtConnectionErrorRoute
import uk.gov.onelogin.sharing.holder.cancellation.HolderCancellationScreenRoute
import uk.gov.onelogin.sharing.holder.consent.HolderConsentRoute
import uk.gov.onelogin.sharing.holder.error.UnrecoverableHolderErrorRoute
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesRoute
import uk.gov.onelogin.sharing.holder.prerequisites.retry.RetryHolderPrerequisitesRoute
import uk.gov.onelogin.sharing.holder.presentation.HolderPresentQrRoute

class HolderRouteParameters : TestParametersValuesProvider() {
    private val inputs = listOf<Triple<String, Any, TestNavHostController.() -> Boolean>>(
        Triple(
            "Holder prerequisites route",
            HolderPrerequisitesRoute
        ) {
            instanceOf<HolderPrerequisitesRoute>(
                HolderPrerequisitesRoute::class.java
            ).matches(
                currentBackStackEntry?.toRoute<HolderPrerequisitesRoute>()
            )
        },
        Triple(
            "Holder unrecoverable error",
            UnrecoverableHolderErrorRoute
        ) {
            instanceOf<UnrecoverableHolderErrorRoute>(
                UnrecoverableHolderErrorRoute::class.java
            ).matches(
                currentBackStackEntry?.toRoute<UnrecoverableHolderErrorRoute>()
            )
        },
        Triple(
            "Retry holder prerequisites",
            RetryHolderPrerequisitesRoute
        ) {
            instanceOf<RetryHolderPrerequisitesRoute>(
                RetryHolderPrerequisitesRoute::class.java
            ).matches(
                currentBackStackEntry?.toRoute<RetryHolderPrerequisitesRoute>()
            )
        },
        Triple(
            "Holder present QR route",
            HolderPresentQrRoute
        ) {
            instanceOf<HolderPresentQrRoute>(
                HolderPresentQrRoute::class.java
            ).matches(
                currentBackStackEntry?.toRoute<HolderPresentQrRoute>()
            )
        },
        Triple(
            "Holder consent confirmation",
            HolderConsentRoute
        ) {
            instanceOf<HolderConsentRoute>(
                HolderConsentRoute::class.java
            ).matches(
                currentBackStackEntry?.toRoute<HolderConsentRoute>()
            )
        },
        Triple(
            "Holder bluetooth connection error",
            BtConnectionErrorRoute("This is a unit test")
        ) {
            instanceOf<BtConnectionErrorRoute>(
                BtConnectionErrorRoute::class.java
            ).matches(
                currentBackStackEntry?.toRoute<BtConnectionErrorRoute>()
            )
        },
        Triple(
            "Holder cancellation screen",
            HolderCancellationScreenRoute
        ) {
            HolderCancellationScreenRoute::class.java == currentBackStackEntry
                ?.toRoute<HolderCancellationScreenRoute>()?.javaClass
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
