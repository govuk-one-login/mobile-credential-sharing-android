package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.holder.R.string.more_about_dvs_privacy_policy_link
import uk.gov.onelogin.sharing.holder.R.string.more_about_dvs_title

object MoreAboutDvsTestData {
    /**
     * Doesn't use the android `Context` so that resource strings must stay as the right value.
     *
     * Also handles the parameter of the related [more_about_dvs_title] resource.
     */
    val expectedScreenEvent = Screen(
        name = "More about DVS",
        id = "288470f5-8bdb-43cf-8cd7-38bcd241413b",
        params = walletSharingRequiredParameters
    ).asLegacyEvent()

    /**
     * Doesn't use the android `Context` so that resource strings must stay as the right value.
     *
     * Also handles the parameter of the related [more_about_dvs_privacy_policy_link] resource.
     */
    val expectedButtonEvent = Button(
        text = "DVS’s privacy notice",
        params = walletSharingRequiredParameters
    ).asLegacyEvent()
}
