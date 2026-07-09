package uk.gov.onelogin.sharing.orchestration.holder.session

class FakeInboundMessageClassifier : InboundMessageClassifier {
    var typeToReturn: InboundMessageType = InboundMessageType.SessionEstablishment
    var getMessageTypeCalls = 0

    override fun getMessageType(rawBytes: ByteArray): InboundMessageType {
        getMessageTypeCalls++
        return typeToReturn
    }
}
