package uk.gov.onelogin.sharing.security.cose

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec

/**
 * Represents a COSE Key, specifically formatted for Elliptic Curve keys (EC2).
 * This data class holds the essential parameters required to represent an
 * uncompressed public key as defined by COSE standards.
 *
 * @param keyType The COSE key type. Defaults to `Cose.KEY_TYPE_EC2` (Elliptic Curve).
 * @param curve The identifier for the elliptic curve, e.g., `Cose.CURVE_P256`.
 * @param x The 32-byte array representing the x-coordinate of the public key.
 * @param y The 32-byte array representing the y-coordinate of the public key.
 */

const val THIRTY_TWO_BYTES = 32
const val THIRTY_THREE_BYTES = 33

data class CoseKey(
    val keyType: Long = Cose.KEY_TYPE_EC2,
    val curve: Long,
    val x: ByteArray,
    val y: ByteArray
) {

    /**
     * This function extracts the X and Y coordinates from the given public key,
     * pads them to the required 32-byte length for the P-256 curve, and constructs
     * a [CoseKey] object.
     *
     * @param publicKey The standard [ECPublicKey] to convert.
     * @return A [CoseKey] instance representing the provided public key.
     */
    companion object {
        fun generateCoseKey(publicKey: ECPublicKey): CoseKey {
            val xCoord = padEcCoordinatesTo32Bytes(publicKey.w.affineX)
            val yCoord = padEcCoordinatesTo32Bytes(publicKey.w.affineY)

            return CoseKey(
                curve = Cose.CURVE_P256,
                x = xCoord,
                y = yCoord
            )
        }

        /**
         * Handles a BigInteger coordinate from an EC key into a fixed 32-byte array.
         *
         * This function is necessary because `BigInteger.toByteArray()` can produce variable-length
         * arrays. It handles three main cases:
         * 1. If the array is 33 bytes with a leading zero it removes the zero.
         * 2. If the array is less than or equal to 32 bytes, it left-pads it with zeros to ensure a 32-byte length.
         * 3. For any other size it takes the last 32 bytes
         *
         * @param coord The [BigInteger] X Y coordinate to pad.
         * @return A [ByteArray] of exactly 32 bytes.
         */
        fun padEcCoordinatesTo32Bytes(coord: BigInteger): ByteArray {
            val bytes = coord.toByteArray()
            return if (bytes.size == THIRTY_THREE_BYTES && bytes[0] == 0.toByte()) {
                bytes.copyOfRange(1, THIRTY_THREE_BYTES)
            } else if (bytes.size <= THIRTY_TWO_BYTES) {
                val padded = ByteArray(THIRTY_TWO_BYTES)
                System.arraycopy(bytes, 0, padded, THIRTY_TWO_BYTES - bytes.size, bytes.size)
                padded
            } else {
                bytes.copyOfRange(bytes.size - THIRTY_TWO_BYTES, bytes.size)
            }
        }

        /**
         * Parses a COSE public key from its CBOR byte representation into a standard
         * [ECPublicKey] object.
         *
         * @param eReaderBytes The raw [ByteArray] of the CBOR-encoded COSE public key.
         * @return An [ECPublicKey] instance corresponding to the input bytes.
         * @throws IllegalArgumentException if the input is not a valid CBOR object, or if the
         *         X or Y coordinates are missing from the COSE key structure.
         */
        fun parseEReaderPublicKey(eReaderBytes: ByteArray): ECPublicKey {
            val cborMapper = CBORMapper()
            val node = cborMapper.readTree(eReaderBytes) as? ObjectNode
                ?: throw IllegalArgumentException("Invalid COSE key")

            val xBytesRaw = node.get("-2")?.binaryValue()
            val yBytesRaw = node.get("-3")?.binaryValue()

            val xBytes = padEcCoordinatesTo32Bytes(BigInteger(1, xBytesRaw))
            val yBytes = padEcCoordinatesTo32Bytes(BigInteger(1, yBytesRaw))

            val x = BigInteger(1, xBytes)
            val y = BigInteger(1, yBytes)

            val params = AlgorithmParameters.getInstance("EC").apply {
                init(ECGenParameterSpec("secp256r1"))
            }
            val ecSpec = params.getParameterSpec(java.security.spec.ECParameterSpec::class.java)

            val pubSpec = ECPublicKeySpec(ECPoint(x, y), ecSpec)
            return KeyFactory.getInstance("EC").generatePublic(pubSpec) as ECPublicKey
        }
    }
}
