import logging
from logging518 import config as logging_config
import os
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import ec

logging_config.fileConfig("pyproject.toml")
logger = logging.getLogger("project")


class KeyGenerator:
    """Handles loading and generating EC private keys."""

    def load_or_create_pem(self, path: str) -> ec.EllipticCurvePrivateKey:
        """
        Loads an EC private key from a PEM file at `path`.
        If the file does not exist, generates a new P-256 key, saves it, and returns it.
        """
        if os.path.exists(path):
            return self.load_pem(path)

        logger.info(f"'{path}' not found! Creating...")
        key = self.generate()
        with open(path, "xb") as f:
            f.write(
                key.private_bytes(
                    encoding=serialization.Encoding.PEM,
                    format=serialization.PrivateFormat.PKCS8,
                    encryption_algorithm=serialization.NoEncryption(),
                )
            )
        return key

    def load_pem(self, path: str) -> ec.EllipticCurvePrivateKey:
        """Loads an EC private key from a PEM file at `path`."""
        with open(path, "rb") as f:
            logger.info(f"Loading key: {path}")
            return serialization.load_pem_private_key(f.read(), password=None)

    def generate(self) -> ec.EllipticCurvePrivateKey:
        """Generates a new P-256 EC private key."""
        return ec.generate_private_key(ec.SECP256R1())
