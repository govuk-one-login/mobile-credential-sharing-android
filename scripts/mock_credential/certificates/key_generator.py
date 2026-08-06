import os
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import ec


class KeyGenerator:
    """Handles loading and generating EC private keys."""

    @staticmethod
    def load_or_create(path: str) -> ec.EllipticCurvePrivateKey:
        """
        Loads an EC private key from a PEM file at `path`.
        If the file does not exist, generates a new P-256 key, saves it, and returns it.
        """
        if os.path.exists(path):
            with open(path, "rb") as f:
                return serialization.load_pem_private_key(f.read(), password=None)

        print(f"'{path}' not found! Creating...")
        key = KeyGenerator.generate()
        with open(path, "xb") as f:
            f.write(key.private_bytes(
                encoding=serialization.Encoding.PEM,
                format=serialization.PrivateFormat.PKCS8,
                encryption_algorithm=serialization.NoEncryption()
            ))
        return key

    @staticmethod
    def load(path: str) -> ec.EllipticCurvePrivateKey:
        """Loads an EC private key from a PEM file at `path`."""
        with open(path, "rb") as f:
            return serialization.load_pem_private_key(f.read(), password=None)

    @staticmethod
    def generate() -> ec.EllipticCurvePrivateKey:
        """Generates a new P-256 EC private key."""
        return ec.generate_private_key(ec.SECP256R1())
