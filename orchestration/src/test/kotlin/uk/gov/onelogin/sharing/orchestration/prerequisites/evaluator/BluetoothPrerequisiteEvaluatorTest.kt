package uk.gov.onelogin.sharing.orchestration.prerequisites.evaluator

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.UserManager
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Before
import uk.gov.onelogin.sharing.core.permission.FakePermissionChecker
import uk.gov.onelogin.sharing.core.permission.PermissionCheckerV2
import uk.gov.onelogin.sharing.core.permission.PermissionsToResultExt.toDeniedPermission
import uk.gov.onelogin.sharing.core.permission.PermissionsToResultExt.toPermanentlyDeniedPermissions
import uk.gov.onelogin.sharing.core.permission.PermissionsToResultExt.toUndeterminedPermissions
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState

class BluetoothPrerequisiteEvaluatorTest {

    private val context: Context = mockk()
    private val bluetoothManager: BluetoothManager = mockk()
    private val bluetoothAdapter: BluetoothAdapter = mockk()
    private val userManager: UserManager = mockk()
    private val permissions = listOf(
        "android.permission.BLUETOOTH",
        "android.permission.BLUETOOTH_SCAN"
    )

    private var permissionResult: MutableList<PermissionCheckerV2.PermissionCheckResult> =
        mutableListOf()

    private val evaluator by lazy {
        BluetoothPrerequisiteEvaluator(
            context = context,
            permissionChecker = FakePermissionChecker { permissionResult }
        )
    }

    @Before
    fun setUp() {
        every { context.getSystemService(Context.BLUETOOTH_SERVICE) } returns bluetoothManager
        every { context.getSystemService(Context.USER_SERVICE) } returns userManager
        every { bluetoothManager.adapter } returns bluetoothAdapter
        every { userManager.hasUserRestriction(UserManager.DISALLOW_BLUETOOTH) } returns false
        every { bluetoothAdapter.isEnabled } returns true
    }

    @Test
    fun `Returns null when all checks pass`() {
        assertNull(evaluator.evaluate())
    }

    @Test
    fun `Returns PermissionUndetermined from permission checker`() {
        permissions.toUndeterminedPermissions()
            .let(permissionResult::addAll)
        assertEquals(BluetoothState.PermissionUndetermined, evaluator.evaluate())
    }

    @Test
    fun `Returns PermissionNotGranted from permission checker`() {
        permissions.toDeniedPermission()
            .let(permissionResult::addAll)
        assertEquals(BluetoothState.PermissionNotGranted, evaluator.evaluate())
    }

    @Test
    fun `Returns PermissionDeniedPermanently from permission checker`() {
        permissions.toPermanentlyDeniedPermissions()
            .let(permissionResult::addAll)
        assertEquals(BluetoothState.PermissionDeniedPermanently, evaluator.evaluate())
    }

    @Test
    fun `Returns Unsupported when bluetooth adapter is null`() {
        every { bluetoothManager.adapter } returns null
        assertEquals(BluetoothState.Unsupported, evaluator.evaluate())
    }

    @Test
    fun `Returns Restricted when user restriction is set`() {
        every { userManager.hasUserRestriction(UserManager.DISALLOW_BLUETOOTH) } returns true
        assertEquals(BluetoothState.Restricted, evaluator.evaluate())
    }

    @Test
    fun `Returns PoweredOff when adapter is disabled`() {
        every { bluetoothAdapter.isEnabled } returns false
        assertEquals(BluetoothState.PoweredOff, evaluator.evaluate())
    }

    @Test
    fun `Permission check takes priority over support check`() {
        permissions.toDeniedPermission()
            .let(permissionResult::addAll)
        every { bluetoothManager.adapter } returns null
        assertEquals(BluetoothState.PermissionNotGranted, evaluator.evaluate())
    }

    @Test
    fun `Support check takes priority over restriction check`() {
        every { bluetoothManager.adapter } returns null
        every { userManager.hasUserRestriction(UserManager.DISALLOW_BLUETOOTH) } returns true
        assertEquals(BluetoothState.Unsupported, evaluator.evaluate())
    }

    @Test
    fun `Restriction check takes priority over readiness check`() {
        every { userManager.hasUserRestriction(UserManager.DISALLOW_BLUETOOTH) } returns true
        every { bluetoothAdapter.isEnabled } returns false
        assertEquals(BluetoothState.Restricted, evaluator.evaluate())
    }
}
