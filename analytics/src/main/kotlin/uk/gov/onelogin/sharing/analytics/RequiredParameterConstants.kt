package uk.gov.onelogin.sharing.analytics

import uk.gov.logging.api.analytics.parameters.data.AnalyticsData
import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel2
import uk.gov.logging.api.v3dot1.model.RequiredParameters

object RequiredParameterConstants {
    private val proximitySharingTaxonomy = object : AnalyticsData.Taxonomy3 {
        override val value: String
            get() = "proximity sharing"
    }
    val walletSharingRequiredParameters = RequiredParameters(
        taxonomyLevel2 = TaxonomyLevel2.WALLET,
        taxonomyLevel3 = proximitySharingTaxonomy
    )
}
