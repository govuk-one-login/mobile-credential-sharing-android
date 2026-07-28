class IssuerAuthInput:

    def __init__(self, **kwargs):
        self.private_key = kwargs["private_key"]
        self.issuer_private_key = kwargs["issuer_private_key"]
        self.x509_certificate = kwargs["x509_certificate"]
        self.output = kwargs["output"]
        self.validity_days = kwargs["validity_days"]
