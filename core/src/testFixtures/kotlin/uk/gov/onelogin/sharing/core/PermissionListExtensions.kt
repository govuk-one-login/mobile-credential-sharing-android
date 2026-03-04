package uk.gov.onelogin.sharing.core

import androidx.test.rule.GrantPermissionRule

object PermissionListExtensions {
    fun Collection<String>.toGrantPermissionsRule(): GrantPermissionRule =
        GrantPermissionRule.grant(
            *this.toTypedArray()
        )
}
