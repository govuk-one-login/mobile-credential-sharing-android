import logging
from logging518 import config as logging_config
import os
from abc import ABCMeta, abstractmethod
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives.asymmetric.types import PrivateKeyTypes

logging_config.fileConfig("pyproject.toml")
logger = logging.getLogger("project")


class KeyGenerator(metaclass=ABCMeta):
    """Handles loading and generating EC private keys."""

    def load_or_create(self, path: str) -> PrivateKeyTypes:
        """
        Loads an EC private key from a PEM file at `path`.
        If the file does not exist, generates a new P-256 key, saves it, and returns it.
        """
        if os.path.exists(path):
            return self.load(path)

        logger.info(f"'{path}' not found! Creating...")
        key = self.generate()
        self.write_to_file(path, key)
        return key

    @abstractmethod
    def write_to_file(self, path: str, key: PrivateKeyTypes):
        pass

    @abstractmethod
    def load(self, path: str) -> PrivateKeyTypes:
        """Loads an EC private key from a PEM file at `path`."""
        pass

    def generate(self) -> ec.EllipticCurvePrivateKey:
        """Generates a new P-256 EC private key."""
        return ec.generate_private_key(ec.SECP256R1())


class PemKeyGenerator(KeyGenerator):
    """Handles loading and generating EC private keys."""

    def write_to_file(self, path: str, key: PrivateKeyTypes):
        with open(path, "xb") as f:
            f.write(
                key.private_bytes(
                    encoding=serialization.Encoding.PEM,
                    format=serialization.PrivateFormat.PKCS8,
                    encryption_algorithm=serialization.NoEncryption(),
                )
            )

    def load(self, path: str) -> PrivateKeyTypes:
        """Loads an EC private key from a PEM file at `path`."""
        with open(path, "rb") as f:
            logger.info(f"Loading key: {path}")
            return serialization.load_pem_private_key(f.read(), password=None)


class DerKeyGenerator(KeyGenerator):
    """Handles loading and generating EC private keys."""

    def write_to_file(self, path: str, key: PrivateKeyTypes):
        with open(path, "xb") as f:
            f.write(
                key.private_bytes(
                    encoding=serialization.Encoding.DER,
                    format=serialization.PrivateFormat.PKCS8,
                    encryption_algorithm=serialization.NoEncryption(),
                )
            )

    def load(self, path: str) -> PrivateKeyTypes:
        """Loads an EC private key from a PEM file at `path`."""
        with open(path, "rb") as f:
            logger.info(f"Loading key: {path}")
            return serialization.load_der_private_key(f.read(), password=None)
