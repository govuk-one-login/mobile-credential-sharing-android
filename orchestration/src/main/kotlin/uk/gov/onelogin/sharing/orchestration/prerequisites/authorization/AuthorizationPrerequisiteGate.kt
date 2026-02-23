package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.core.permission.PermissionChecker
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGate

@ContributesBinding(
    AppScope::class,
    binding = binding<PrerequisiteGate.Authorization>()
)
class AuthorizationPrerequisiteGate(
    permissionChecker: PermissionChecker,
    private val logger: Logger
) : PrerequisiteGate.Authorization,
    PermissionChecker by permissionChecker {

    override fun checkAuthorization(request: AuthorizationRequest): AuthorizationResponse {
        logger.debug(
            logTag,
            "Received authorization request: $request"
        )

        return when (request) {
            is AuthorizationRequest.AuthorizePermission -> checkPermissions(
                request.permissions
            ).let(::handlePermissionResponse)
        }.also {
            logger.debug(
                logTag,
                "Completed authorization request. Response: $it"
            )
        }
    }

    private fun handlePermissionResponse(
        result: PermissionChecker.Response
    ): AuthorizationResponse = when (result) {
        PermissionChecker.Response.Passed -> AuthorizationResponse.Authorized

        is PermissionChecker.Response.Missing -> AuthorizationResponse.Unauthorized(
            UnauthorizedReason.MissingPermissions(
                result.missingPermissions
            )
        )
    }
}
