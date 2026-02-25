package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization

import android.Manifest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.core.permission.PermissionChecker
import uk.gov.onelogin.sharing.core.permission.StubPermissionChecker
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.matchers.AuthorizationResponseMatchers.hasUnauthorizedPermissions

class AuthorizationPrerequisiteGateTest {

    private val logger = SystemLogger()
    private val permission = Manifest.permission.CAMERA
    private val request = AuthorizationRequest.AuthorizePermission(permission)
    private var permissionResult: PermissionChecker.Response = PermissionChecker.Response.Passed
    private val permissionChecker by lazy {
        StubPermissionChecker(
            permissionResult
        )
    }

    private val gate by lazy {
        AuthorizationPrerequisiteGate(
            logger = logger,
            permissionChecker = permissionChecker
        )
    }

    @Test
    fun `Converts a successful permissions check into an Authorized response`() = runTest {
        val result = gate.checkAuthorization(request)

        assert("Received authorization request: $request" in logger)
        assert("Completed authorization request. Response: $result" in logger)
        assertThat(
            result,
            equalTo(AuthorizationResponse.Authorized)
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
