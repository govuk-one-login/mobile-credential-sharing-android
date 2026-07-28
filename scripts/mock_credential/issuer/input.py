from argparse import ArgumentParser, Namespace
from typing import TypeVar, Type

T = TypeVar('T', bound='Parent') # type: ignore

class IssuerAuthInput:

    def __init__(self, **kwargs):
        self.issuer_private_key = kwargs["issuer_private_key"]
        self.output = kwargs["output"]
        self.private_key = kwargs["private_key"]
        self.validity_days = kwargs["validity_days"]
        self.x509_certificate = kwargs["x509_certificate"]

    def __eq__(self, other):
        return (
            isinstance(other, IssuerAuthInput)
            and (self.issuer_private_key == other.issuer_private_key)
            and (self.output == other.output)
            and (self.private_key == other.private_key)
            and (self.validity_days == other.validity_days)
            and (self.x509_certificate == other.x509_certificate)
        )

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
            issuer_private_key = args.issuer_private_key,
            output = args.output,
            private_key = args.private_key,
            validity_days = args.validity_days,
            x509_certificate = args.x509_certificate,
        )