package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization

import android.Manifest
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker.Companion.bluetoothPermissions
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.core.permission.PermissionChecker
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGateLayer

@ContributesBinding(
    AppScope::class,
    binding = binding<PrerequisiteGateLayer.Authorization>()
)
class AuthorizationPrerequisiteGateLayer(
    permissionChecker: PermissionChecker,
    private val logger: Logger
) : PrerequisiteGateLayer.Authorization,
    PermissionChecker by permissionChecker {

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
            Prerequisite.UNKNOWN -> emptyList()
        }

    private fun handlePermissionResponse(
        result: PermissionChecker.Response
    ): MissingPrerequisiteReason.Unauthorized? = when (result) {
        PermissionChecker.Response.Passed -> null

        is PermissionChecker.Response.Missing -> MissingPrerequisiteReason.Unauthorized(
            UnauthorizedReason.MissingPermissions(
                result.missingPermissions.toSet()
            )
        )
    }
}
