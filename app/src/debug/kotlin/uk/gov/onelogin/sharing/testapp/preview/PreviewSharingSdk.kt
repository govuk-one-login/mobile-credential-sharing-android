package uk.gov.onelogin.sharing.uk.gov.onelogin.sharing.testapp.preview

import uk.gov.onelogin.sharing.SharingSdk
import uk.gov.onelogin.sharing.di.SharingAppGraph

class PreviewSharingSdk : SharingSdk {
    override val appGraph: SharingAppGraph = PreviewSharingAppGraph()
}
