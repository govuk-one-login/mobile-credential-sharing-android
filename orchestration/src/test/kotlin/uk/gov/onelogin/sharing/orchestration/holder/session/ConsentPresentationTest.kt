package uk.gov.onelogin.sharing.orchestration.holder.session

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class ConsentPresentationTest {

    private fun presentation(organizationName: String?) = ConsentPresentation(
        documents = emptyList(),
        organizationName = organizationName
    )

    @Test
    fun `normalisedOrganizationName is null when organisation name is null`() {
        assertNull(presentation(organizationName = null).normalisedOrganizationName)
    }

    @Test
    fun `normalisedOrganizationName is null when organisation name is blank`() {
        assertNull(presentation(organizationName = "   ").normalisedOrganizationName)
    }

    @Test
    fun `normalisedOrganizationName appends a full stop when the name has none`(
        @TestParameter orgName: String = testValues(
            "Organisation Ltd",
            "Organisation Ltd.",
            "Organisation Ltd... ",
            "  Organisation Ltd  ",
            "Organisation Ltd ."
        )
    ) {
        assertEquals(
            "Organisation Ltd.",
            presentation(organizationName = orgName).normalisedOrganizationName
        )
    }
}
