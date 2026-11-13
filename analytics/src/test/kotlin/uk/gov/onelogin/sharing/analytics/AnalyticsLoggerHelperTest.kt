package uk.gov.onelogin.sharing.analytics

import java.util.UUID
import kotlin.test.Test
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel2
import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.RequiredParameters
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Error
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.buttonEvent
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.errorScreenEvent
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.screenEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

class AnalyticsLoggerHelperTest {

    private val analytics by lazy {
        FakeAnalyticsLogger()
    }

    private val text = "This is a unit test"
    private val id = UUID.randomUUID().toString()
    private val reason = "Testing purposes"
    private var requiredParameters = walletSharingRequiredParameters
    private var endpoint = ""
    private var status = ""

    private val expectedButtonEvent by lazy {
        Button(
            text = text,
            params = requiredParameters
        ).asLegacyEvent()
    }

    private val expectedErrorScreenEvent by lazy {
        Error(
            endpoint = endpoint,
            id = id,
            name = text,
            params = requiredParameters,
            reason = reason,
            status = status
        ).asLegacyEvent()
    }

    private val expectedScreenEvent by lazy {
        Screen(
            name = text,
            id = id,
            params = requiredParameters
        ).asLegacyEvent()
    }

    @Test
    fun `Button events with default parameters defer to wallet sharing parameters`() {
        analytics.buttonEvent(
            text = text
        )

        assertThat(
            analytics,
            contains(expectedButtonEvent)
        )
    }

    @Test
    fun `Button event required parameters are configurable`() {
        requiredParameters = RequiredParameters(
            taxonomyLevel2 = TaxonomyLevel2.DOCUMENT_CHECKING_APP
        )

        analytics.buttonEvent(
            text = text,
            params = requiredParameters
        )

        assertThat(
            analytics,
            contains(expectedButtonEvent)
        )
    }

    @Test
    fun `Error events with default parameters defer to wallet sharing parameters`() {
        analytics.errorScreenEvent(
            name = text,
            id = id,
            reason = reason
        )

        assertThat(
            analytics,
            contains(expectedErrorScreenEvent)
        )
    }

    @Test
    fun `Error event endpoints are configurable`() {
        endpoint = "An endpoint"

        analytics.errorScreenEvent(
            name = text,
            id = id,
            reason = reason,
            endpoint = endpoint
        )

        assertThat(
            analytics,
            contains(expectedErrorScreenEvent)
        )
    }

    @Test
    fun `Error event required parameters are configurable`() {
        requiredParameters = RequiredParameters(
            taxonomyLevel2 = TaxonomyLevel2.DOCUMENT_CHECKING_APP
        )

        analytics.errorScreenEvent(
            name = text,
            id = id,
            reason = reason,
            params = requiredParameters
        )

        assertThat(
            analytics,
            contains(expectedErrorScreenEvent)
        )
    }

    @Test
    fun `Error event statuses are configurable`() {
        status = "500"

        analytics.errorScreenEvent(
            name = text,
            id = id,
            reason = reason,
            status = status
        )

        assertThat(
            analytics,
            contains(expectedErrorScreenEvent)
        )
    }

    @Test
    fun `Screen events with default parameters defer to wallet sharing parameters`() {
        analytics.screenEvent(
            title = text,
            id = id
        )

        assertThat(
            analytics,
            contains(expectedScreenEvent)
        )
    }

    @Test
    fun `Screen event required parameters are configurable`() {
        requiredParameters = RequiredParameters(
            taxonomyLevel2 = TaxonomyLevel2.DOCUMENT_CHECKING_APP
        )

        analytics.screenEvent(
            title = text,
            id = id,
            params = requiredParameters
        )

        assertThat(
            analytics,
            contains(expectedScreenEvent)
        )
    }
}
