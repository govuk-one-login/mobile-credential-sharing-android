package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization

import android.Manifest
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker.Companion.bluetoothPermissions
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.core.permission.IterablePermissionsExt.toPermissionsList
import uk.gov.onelogin.sharing.core.permission.PermissionCheckerV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGateLayer

@ContributesBinding(
    AppScope::class,
    binding = binding<PrerequisiteGateLayer.Authorization>()
)
class AuthorizationPrerequisiteGateLayer(
    permissionChecker: PermissionCheckerV2,
    private val logger: Logger
) : PrerequisiteGateLayer.Authorization,
    PermissionCheckerV2 by permissionChecker {

    override fun checkAuthorization(
        prerequisite: Prerequisite
    ): MissingPrerequisiteReason.Unauthorized? = calculatePermissions(prerequisite)
        .let(::checkPermissions)
        .let(::handlePermissionResponse)
        .also {
            logger.debug(
                logTag,
                "Performed $prerequisite authorization check. Response: $it"
            )
        }

    private fun calculatePermissions(prerequisite: Prerequisite): List<String> =
        when (prerequisite) {
            Prerequisite.BLUETOOTH -> bluetoothPermissions()

            Prerequisite.CAMERA -> listOf(Manifest.permission.CAMERA)

            Prerequisite.LOCATION,
            Prerequisite.UNKNOWN
            -> emptyList()
        }

    private fun handlePermissionResponse(
        result: List<PermissionCheckerV2.Denied>
    ): MissingPrerequisiteReason.Unauthorized? = if (result.isEmpty()) {
        null
    } else {
        MissingPrerequisiteReason.Unauthorized(
            UnauthorizedReason.MissingPermissions(
                result.toPermissionsList().toSet()
            )
        )
    }
}
