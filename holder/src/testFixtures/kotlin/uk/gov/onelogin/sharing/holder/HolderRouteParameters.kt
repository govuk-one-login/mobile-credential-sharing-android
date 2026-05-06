package uk.gov.onelogin.sharing.holder

import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BtConnectionErrorRoute
import uk.gov.onelogin.sharing.holder.consent.HolderConsentRoute
import uk.gov.onelogin.sharing.holder.error.UnrecoverableHolderErrorRoute
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesRoute
import uk.gov.onelogin.sharing.holder.prerequisites.retry.RetryHolderPrerequisitesRoute
import uk.gov.onelogin.sharing.holder.presentation.HolderPresentQrRoute

class HolderRouteParameters : TestParametersValuesProvider() {
    private val inputs = listOf<Triple<String, Any, TestNavHostController.() -> Any?>>(
        Triple(
            "Holder prerequisites route",
            HolderPrerequisitesRoute
        ) {
            currentBackStackEntry?.toRoute<HolderPrerequisitesRoute>()
        },
        Triple(
            "Holder unrecoverable error",
            UnrecoverableHolderErrorRoute
        ) {
            currentBackStackEntry?.toRoute<UnrecoverableHolderErrorRoute>()
        },
        Triple(
            "Retry holder prerequisites",
            RetryHolderPrerequisitesRoute
        ) {
            currentBackStackEntry?.toRoute<RetryHolderPrerequisitesRoute>()
        },
        Triple(
            "Holder present QR route",
            HolderPresentQrRoute
        ) {
            currentBackStackEntry?.toRoute<HolderPresentQrRoute>()
        },
        Triple(
            "Holder consent confirmation",
            HolderConsentRoute
        ) {
            currentBackStackEntry?.toRoute<HolderConsentRoute>()
        },
        Triple(
            "Holder bluetooth connection error",
            BtConnectionErrorRoute("This is a unit test")
        ) {
            currentBackStackEntry?.toRoute<BtConnectionErrorRoute>()
        }
    )

    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues?> =
        inputs.map { (name, route, conversion) ->
            TestParameters.TestParametersValues.builder()
                .name(name)
                .addParameter("route", route)
                .addParameter("conversion", conversion)
                .build()
        }
}
