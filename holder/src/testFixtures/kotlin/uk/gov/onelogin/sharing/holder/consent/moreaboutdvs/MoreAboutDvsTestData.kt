package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

object MoreAboutDvsTestData {
    const val SCREEN_NAME = "More about DVS"

    /**
     * Doesn't use the android `Context` so that resource strings must stay as the right value.
     *
     * Also handles the parameter of the related [more_about_dvs_title] resource.
     */
    val expectedScreenEvent = Screen(
        name = SCREEN_NAME,
        id = "288470f5-8bdb-43cf-8cd7-38bcd241413b",
        params = walletSharingRequiredParameters
    ).asLegacyEvent()

    const val PRIVACY_NOTICE_BUTTON_TEXT = "DVS’s privacy notice"

    /**
     * Doesn't use the android `Context` so that resource strings must stay as the right value.
     *
     * Also handles the parameter of the related [more_about_dvs_privacy_policy_link] resource.
     */
    val expectedButtonEvent = Button(
        text = PRIVACY_NOTICE_BUTTON_TEXT,
        params = walletSharingRequiredParameters
    ).asLegacyEvent()
}
