package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector

/**
 * Covers invariants that apply across the whole [IssuerRootOption] set, plus the specific
 * mapping for options that must resolve to a known asset (for example, the DVLA integration root).
 *
 * The per-option invariants live in [IssuerRootOptionTest]; these aggregate assertions are kept
 * separate so they run once over all entries rather than once per injected parameter.
 */
@RunWith(RobolectricTestParameterInjector::class)
class IssuerRootOptionMappingTest {

    @Test
    fun `Display names are unique across options`() = runTest {
        val displayNames = IssuerRootOption.entries.map { it.displayName }

        assertEquals(
            displayNames.size,
            displayNames.distinct().size,
            "Each IssuerRootOption must have a unique display name"
        )
    }

    @Test
    fun `Root certificate assets are unique across options`() = runTest {
        val assets = IssuerRootOption.entries.map { it.certificateAsset }

        assertEquals(
            assets.size,
            assets.distinct().size,
            "Each IssuerRootOption must map to a unique certificate asset"
        )
    }

    @Test
    fun `DVLA integration option maps to the DVLA integration der asset`() = runTest {
        assertEquals(
            "dvla_integration_x509_certificate.der",
            IssuerRootOption.DVLA_ROOT_INTEGRATION.certificateAsset
        )
    }

    @Test
    fun `DVLA integration option has the expected display name`() = runTest {
        assertEquals(
            "DVLA (Integration)",
            IssuerRootOption.DVLA_ROOT_INTEGRATION.displayName
        )
    }
}
