from cryptography import x509

ISSUER_NAME = x509.Name([
    x509.NameAttribute(x509.NameOID.COUNTRY_NAME, "GB"),
    x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, "London"),
    x509.NameAttribute(x509.NameOID.COMMON_NAME, "mDoc Test Issuer"),
    x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, "DVLA Dev Tool"),
])

LEAF_NAME = x509.Name([
    x509.NameAttribute(x509.NameOID.COUNTRY_NAME, "GB"),
    x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, "London"),
    x509.NameAttribute(x509.NameOID.COMMON_NAME, "mDoc Test Leaf"),
    x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, "DVLA Dev Tool"),
])
