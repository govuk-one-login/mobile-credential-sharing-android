package uk.gov.onelogin.sharing.orchestration.prerequisites.evaluator

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.After
import org.junit.Before
import uk.gov.onelogin.sharing.core.permission.PermissionChecker
import uk.gov.onelogin.sharing.core.permission.StubPermissionChecker
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.LocationState

class LocationPrerequisiteEvaluatorTest {

    private val context: Context = mockk()
    private val packageManager: PackageManager = mockk()
    private val locationManager: LocationManager = mockk()

    private var permissionResult: PermissionChecker.Response = PermissionChecker.Response.Passed

    private val evaluator by lazy {
        LocationPrerequisiteEvaluator(
            context = context,
            permissionChecker = StubPermissionChecker(permissionResult)
        )
    }

    @Before
    fun setUp() {
        mockkStatic(LocationManagerCompat::class)
        every { context.packageManager } returns packageManager
        every { context.getSystemService(Context.LOCATION_SERVICE) } returns locationManager
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION) } returns true
        every { LocationManagerCompat.isLocationEnabled(locationManager) } returns true
    }

    @After
    fun tearDown() {
        unmockkStatic(LocationManagerCompat::class)
    }

    @Test
    fun `Returns null when all checks pass`() {
        assertNull(evaluator.evaluate())
    }

    @Test
    fun `Returns PermissionNotGranted when permission is missing`() {
        permissionResult =
            PermissionChecker.Response.Missing("android.permission.ACCESS_FINE_LOCATION")
        assertEquals(LocationState.PermissionNotGranted, evaluator.evaluate())
    }

    @Test
    fun `Returns Unsupported when device has no location feature`() {
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION) } returns false
        assertEquals(LocationState.Unsupported, evaluator.evaluate())
    }

    @Test
    fun `Returns ServicesDisabled when location services are off`() {
        every { LocationManagerCompat.isLocationEnabled(locationManager) } returns false
        assertEquals(LocationState.ServicesDisabled, evaluator.evaluate())
    }

    @Test
    fun `Permission check takes priority over support check`() {
        permissionResult =
            PermissionChecker.Response.Missing("android.permission.ACCESS_FINE_LOCATION")
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION) } returns false
        assertEquals(LocationState.PermissionNotGranted, evaluator.evaluate())
    }

    @Test
    fun `Support check takes priority over readiness check`() {
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION) } returns false
        every { LocationManagerCompat.isLocationEnabled(locationManager) } returns false
        assertEquals(LocationState.Unsupported, evaluator.evaluate())
    }
}
