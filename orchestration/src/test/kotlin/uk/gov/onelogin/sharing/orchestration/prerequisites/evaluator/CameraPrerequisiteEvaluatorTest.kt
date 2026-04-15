package uk.gov.onelogin.sharing.orchestration.prerequisites.evaluator

import android.app.admin.DevicePolicyManager
import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import io.mockk.every
import io.mockk.mockk
import java.util.Collections.singleton
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Before
import uk.gov.onelogin.sharing.core.permission.FakePermissionChecker
import uk.gov.onelogin.sharing.core.permission.PermissionCheckerV2
import uk.gov.onelogin.sharing.core.permission.PermissionsToResultExt.toDeniedPermission
import uk.gov.onelogin.sharing.core.permission.PermissionsToResultExt.toPermanentlyDeniedPermissions
import uk.gov.onelogin.sharing.core.permission.PermissionsToResultExt.toUndeterminedPermissions
import uk.gov.onelogin.sharing.orchestration.prerequisites.camera.ProcessCameraProviderFactory
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.CameraState

class CameraPrerequisiteEvaluatorTest {

    private val context: Context = mockk()
    private val devicePolicyManager: DevicePolicyManager = mockk()
    private val processCameraProvider: ProcessCameraProvider = mockk()

    private var permissionResult: MutableList<PermissionCheckerV2.PermissionCheckResult> =
        mutableListOf()
    private var factory = ProcessCameraProviderFactory { processCameraProvider }

    private val evaluator by lazy {
        CameraPrerequisiteEvaluator(
            context = context,
            factory = factory,
            permissionChecker = FakePermissionChecker { permissionResult }
        )
    }

    @Before
    fun setUp() {
        every { context.getSystemService(Context.DEVICE_POLICY_SERVICE) } returns
            devicePolicyManager
        every { devicePolicyManager.getCameraDisabled(null) } returns false
        every { processCameraProvider.hasCamera(any()) } returns true
    }

    @Test
    fun `Returns null when all checks pass`() {
        assertNull(evaluator.evaluate())
    }

    @Test
    fun `Returns PermissionUndetermined when permission is missing`() {
        singleton("android.permission.CAMERA")
            .toUndeterminedPermissions()
            .let(permissionResult::addAll)
        assertEquals(CameraState.PermissionUndetermined, evaluator.evaluate())
    }

    @Test
    fun `Returns PermissionNotGranted when permission is missing`() {
        singleton("android.permission.CAMERA")
            .toDeniedPermission()
            .let(permissionResult::addAll)
        assertEquals(CameraState.PermissionNotGranted, evaluator.evaluate())
    }

    @Test
    fun `Returns PermissionDeniedPermanently when permission is missing`() {
        singleton("android.permission.CAMERA")
            .toPermanentlyDeniedPermissions()
            .let(permissionResult::addAll)
        assertEquals(CameraState.PermissionDeniedPermanently, evaluator.evaluate())
    }

    @Test
    fun `Returns Unsupported when device has no rear-facing camera`() {
        every { processCameraProvider.hasCamera(any()) } returns false
        assertEquals(CameraState.Unsupported, evaluator.evaluate())
    }

    @Test
    fun `Returns Unsupported when ProcessCameraProvider throws`() {
        factory = ProcessCameraProviderFactory { throw IllegalStateException("test") }
        assertEquals(CameraState.Unsupported, evaluator.evaluate())
    }

    @Test
    fun `Returns Restricted when camera is disabled by device policy`() {
        every { devicePolicyManager.getCameraDisabled(null) } returns true
        assertEquals(CameraState.Restricted, evaluator.evaluate())
    }

    @Test
    fun `Permission check takes priority over support check`() {
        singleton("android.permission.CAMERA")
            .toDeniedPermission()
            .let(permissionResult::addAll)
        every { processCameraProvider.hasCamera(any()) } returns false
        assertEquals(CameraState.PermissionNotGranted, evaluator.evaluate())
    }

    @Test
    fun `Support check takes priority over restriction check`() {
        every { processCameraProvider.hasCamera(any()) } returns false
        every { devicePolicyManager.getCameraDisabled(null) } returns true
        assertEquals(CameraState.Unsupported, evaluator.evaluate())
    }
}
