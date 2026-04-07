package uk.gov.onelogin.sharing.orchestration.prerequisites.evaluator

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.FEATURE_LOCATION
import androidx.core.location.LocationManagerCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import uk.gov.onelogin.sharing.bluetooth.ContextExt.locationManager
import uk.gov.onelogin.sharing.core.permission.PermissionChecker
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.LocationState

@ContributesBinding(AppScope::class, binding = binding<PrerequisiteEvaluator<LocationState>>())
@Inject
class LocationPrerequisiteEvaluator(
    private val context: Context,
    permissionChecker: PermissionChecker
) : PermissionChecker by permissionChecker,
    PrerequisiteEvaluator<LocationState> {
    override fun evaluate(): LocationState? = evaluatePermissions()
        ?: evaluateSupport()
        ?: evaluateReadiness()

    private fun evaluatePermissions(): LocationState? =
        checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION).let { result ->
            when (result) {
                PermissionChecker.Response.Passed -> null
                is PermissionChecker.Response.Missing -> LocationState.PermissionNotGranted
            }
        }

    private fun evaluateSupport(): LocationState? = if (
        context.packageManager.hasSystemFeature(FEATURE_LOCATION)
    ) {
        null
    } else {
        LocationState.Unsupported
    }

    private fun evaluateReadiness(): LocationState? = if (
        context.locationManager?.let(LocationManagerCompat::isLocationEnabled) ?: false
    ) {
        null
    } else {
        LocationState.ServicesDisabled
    }
}
