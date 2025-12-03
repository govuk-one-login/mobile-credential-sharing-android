package uk.gov.onelogin.sharing.core.res.values

import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.core.R

/**
 * Test data used with [StringsXmlTest] for resource IDs that explicitly state a value.
 *
 * Ensures that tests would fail when strings update without updating this test data.
 */
enum class StringsXmlData(val expected: String, @param:StringRes val resourceId: Int) {
    PERMISSION_STATE_DENIED(
        expected = "Denied",
        resourceId = R.string.permission_state_denied
    )
}
