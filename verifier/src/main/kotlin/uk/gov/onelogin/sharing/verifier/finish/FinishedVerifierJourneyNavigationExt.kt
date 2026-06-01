package uk.gov.onelogin.sharing.verifier.finish

import android.os.Bundle
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlin.reflect.typeOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse

object FinishedVerifierJourneyNavigationExt {
    fun NavController.navigateToFinishedVerifierJourney(
        response: DeviceResponse,
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(
        FinishedVerifierJourneyRoute(response = response),
        options
    )

    internal fun NavGraphBuilder.configureFinishedVerifierJourney(controller: NavController) {
        composable<FinishedVerifierJourneyRoute>(
            typeMap = mapOf(
                typeOf<DeviceResponse>() to DeviceResponseType
            )
        ) { backStackEntry ->
            val scope = rememberCoroutineScope { Dispatchers.Main }
            val args: FinishedVerifierJourneyRoute = backStackEntry.toRoute()

            FinishedVerifierJourneyScreen(
                response = args.response,
                onExitJourney = {
                    scope.launch {
                        controller.popBackStack()
                    }
                }
            )
        }
    }

    private val DeviceResponseType: NavType<DeviceResponse> =
        object : NavType<DeviceResponse>(
            isNullableAllowed = true
        ) {
            override fun get(bundle: Bundle, key: String): DeviceResponse? =
                bundle.getString(key)?.let { parseValue(it) }

            override fun put(bundle: Bundle, key: String, value: DeviceResponse) {
                bundle.putString(key, serializeAsValue(value))
            }

            override fun parseValue(value: String): DeviceResponse = Json.decodeFromString(value)

            override fun serializeAsValue(value: DeviceResponse): String =
                Json.encodeToString(value)
        }
}
