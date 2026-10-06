package uk.gov.onelogin.sharing.analytics

import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel2
import uk.gov.logging.api.v3dot1.model.RequiredParameters

object RequiredParameterConstants {
    val walletSharingRequiredParameters = RequiredParameters(
        taxonomyLevel2 = TaxonomyLevel2.WALLET
    )
}