package uk.gov.onelogin.sharing.core.implementation

import uk.gov.onelogin.sharing.core.implementation.ImplementationDetailData.implementationDetail

object RequiresImplementationData {
    val requiresImplementation = RequiresImplementation(
        details = emptyArray()
    )
    val requiresImplementationCopy = RequiresImplementation(
        details = emptyArray()
    )
    val requiresImplementationWithDetail = RequiresImplementation(
        details = arrayOf(implementationDetail)
    )
}
