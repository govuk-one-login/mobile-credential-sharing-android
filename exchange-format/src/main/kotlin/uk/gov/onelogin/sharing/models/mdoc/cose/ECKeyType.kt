package uk.gov.onelogin.sharing.models.mdoc.cose

enum class ECKeyType(val id: UInt) {
    OKP(1u),
    EC(2u),
    RSA(3u),
    SYMMETRIC(4u),
    HSS_LMS(5u),
    WALNUT_DSA(6u)
}
