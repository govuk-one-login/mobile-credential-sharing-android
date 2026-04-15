package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization

import android.Manifest
import java.util.Collections.singleton
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.After
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.core.permission.FakePermissionChecker
import uk.gov.onelogin.sharing.core.permission.PermissionCheckerV2
import uk.gov.onelogin.sharing.core.permission.PermissionsToResultExt.toDeniedPermission
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.PrerequisiteResponseMatchers.hasUnauthorizedPermissions

class AuthorizationPrerequisiteGateTest {

    private val logger = SystemLogger()
    private val permission = Manifest.permission.BLUETOOTH
    private val request: Prerequisite = Prerequisite.BLUETOOTH

    private var permissionResult: MutableList<PermissionCheckerV2.PermissionCheckResult> =
        mutableListOf()
    private val permissionChecker by lazy {
        FakePermissionChecker { permissionResult }
    }

    private val gate by lazy {
        AuthorizationPrerequisiteGateLayer(
            logger = logger,
            permissionChecker = permissionChecker
        )
    }

    @After
    fun verifyLogs() {
        assert(
            logger.any {
                it.message.startsWith("Performed $request authorization check.")
            }
        )
    }

    @Test
    fun `Converts a successful permissions check into an Authorized response`() = runTest {
        val result = gate.checkAuthorization(request)

        assertThat(
            result,
            nullValue()
        )
    }

    @Test
    fun `Converts a denied permissions check into an Unauthorized response`() = runTest {
        singleton(permission)
            .toDeniedPermission()
            .let(permissionResult::addAll)

        val result = gate.checkAuthorization(request)

        assertThat(
            result,
            hasUnauthorizedPermissions(
                containsInAnyOrder(permission)
            )
        )
    }
}
