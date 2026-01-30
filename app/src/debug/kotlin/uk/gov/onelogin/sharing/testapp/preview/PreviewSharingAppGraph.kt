package uk.gov.onelogin.sharing.uk.gov.onelogin.sharing.testapp.preview

import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.di.SharingAppGraph

class PreviewSharingAppGraph : SharingAppGraph {

    override fun logger(): Logger = NoOpLogger()
}
