package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment

object SessionEstablishmentStubs {
    private const val DATA_LOCATION =
        "uk/gov/onelogin/sharing/models/mdoc/sessionEstablishment"

    val validSessionEstablishmentDtoBytes: ByteArray = this::class.java.classLoader!!
        .getResourceAsStream(
        "$DATA_LOCATION/sessionEstablishmentExample.txt"
    )!!.bufferedReader().readLines().joinToString("").hexToByteArray()
}
