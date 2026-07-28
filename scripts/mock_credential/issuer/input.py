from argparse import ArgumentParser, Namespace
from typing import TypeVar, Type

T = TypeVar('T', bound='Parent')

class IssuerAuthInput:

    def __init__(self, **kwargs):
        self.private_key = kwargs["private_key"]
        self.issuer_private_key = kwargs["issuer_private_key"]
        self.x509_certificate = kwargs["x509_certificate"]
        self.output = kwargs["output"]
        self.validity_days = kwargs["validity_days"]

    @classmethod
    def from_parser(cls: Type[T], args: Namespace) -> T:
        """
        Create an instance of :class:`IssuerAuthInput` from the provided argument parser.

        Expected arguments from :param:`args`:
        - "private_key"
        - "x509_certificate"
        - "issuer_private_key"
        - "output"
        - "validity_days"
        
        :param cls: The class :class:`Type`. This defers to :class:`IssuerAuthInput`.
        :param args: The parsed arguments from :class:`ArgumentParser`. Generate this via
            :func:`ArgumentParser.parse_args`.
        :return: An instance of :class:`IssuerAuthInput` with the necessary data.
        """

        return cls(
            private_key = args.private_key,
            issuer_private_key = args.issuer_private_key,
            x509_certificate = args.x509_certificate,
            output = args.output,
            validity_days = args.validity_days
        )