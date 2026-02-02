package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

data class ItemsRequest(val docType: String, val nameSpaces: Map<String, Map<String, Boolean>>)
