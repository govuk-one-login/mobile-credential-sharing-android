package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization

import android.Manifest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.After
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.core.permission.PermissionChecker
import uk.gov.onelogin.sharing.core.permission.StubPermissionChecker
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.PrerequisiteResponseMatchers.hasUnauthorizedPermissions

class AuthorizationPrerequisiteGateTest {

    private val logger = SystemLogger()
    private val permission = Manifest.permission.CAMERA
    private val request: Prerequisite = Prerequisite.BLUETOOTH

    private var permissionResult: PermissionChecker.Response = PermissionChecker.Response.Passed
    private val permissionChecker by lazy {
        StubPermissionChecker(
            permissionResult
        )
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
        permissionResult = PermissionChecker.Response.Missing(permission)
        val result = gate.checkAuthorization(request)

        assertThat(
            result,
            hasUnauthorizedPermissions(
                containsInAnyOrder(permission)
            )
        )
    }
}
