package uk.gov.onelogin.sharing.core.permission

fun Iterable<String>.toDeniedPermission(
    shouldShowRationale: Boolean = true
): List<PermissionCheckerV2.Denied> = map {
    PermissionCheckerV2.Denied(
        permission = it,
        shouldShowRationale = shouldShowRationale
    )
}
