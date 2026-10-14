package uk.gov.onelogin.sharing.analytics

import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel2
import uk.gov.logging.api.v3dot1.model.RequiredParameters

object RequiredParameterConstants {
    /**
     * DCMAW-23663:
     * - Update [RequiredParameters.taxonomyLevel3] to be a local implementation of the `Taxonomy3`
     * interface. This requires the associated PR to be merged in.
     */
    val walletSharingRequiredParameters = RequiredParameters(
        taxonomyLevel2 = TaxonomyLevel2.WALLET
    )
}
