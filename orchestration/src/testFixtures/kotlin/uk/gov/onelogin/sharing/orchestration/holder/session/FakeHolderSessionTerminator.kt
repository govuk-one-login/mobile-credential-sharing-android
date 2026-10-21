package uk.gov.onelogin.sharing.orchestration.holder.session

class FakeHolderSessionTerminator : HolderSessionTerminator {
    var terminateCalls = 0

    override suspend fun terminate() {
        terminateCalls++
    }
}
