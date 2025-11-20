package uk.gov.onelogin.sharing.core.implementation

object ImplementationDetailData {
    val implementationDetail = ImplementationDetail()
    val implementationDetailCopy = ImplementationDetail()
    val detailWithTicket = ImplementationDetail(
        ticket = "DCMAW--1"
    )
    val detailWithDescription = ImplementationDetail(
        description = "A unit test"
    )
}
