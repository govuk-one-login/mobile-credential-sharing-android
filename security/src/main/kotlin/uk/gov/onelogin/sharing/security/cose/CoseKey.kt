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
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.security.cbor.deriveUntaggedCbor
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_ALGORITHM
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_PARAMETER_SPEC

const val THIRTY_TWO_BYTES = 32
const val THIRTY_THREE_BYTES = 33

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
data class CoseKey(
    val keyType: Long = Cose.KEY_TYPE_EC2,
    val curve: Long,
    val x: ByteArray,
    val y: ByteArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoseKey

        if (keyType != other.keyType) return false
        if (curve != other.curve) return false
        if (!x.contentEquals(other.x)) return false
        if (!y.contentEquals(other.y)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyType.hashCode()
        result = 31 * result + curve.hashCode()
        result = 31 * result + x.contentHashCode()
        result = 31 * result + y.contentHashCode()
        return result
    }

    /**
     * This function extracts the X and Y coordinates from the given public key,
     * pads them to the required 32-byte length for the P-256 curve, and constructs
     * a [CoseKey] object.
     *
     * @param publicKey The standard [ECPublicKey] to convert.
     * @return A [CoseKey] instance representing the provided public key.
     */
    companion object {
        fun generateCoseKey(publicKey: ECPublicKey, logger: Logger): CoseKey {
            val xCoord = padEcCoordinatesTo32Bytes(publicKey.w.affineX)
            val yCoord = padEcCoordinatesTo32Bytes(publicKey.w.affineY)

            val coseKey = CoseKey(
                curve = Cose.CURVE_P256,
                x = xCoord,
                y = yCoord
            ).also {
                logger.debug(
                    logTag,
                    "Converted EC public key to CoseKey: $it"
                )
            }

            return coseKey
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
         * Parses a CBOR-encoded COSE key to extract the raw elliptic curve (EC) point.
         *
         * This function decodes the provided byte array, expecting it to be a valid
         * COSE_Key structure.
         *
         * @param eReaderBytes The byte array representing the raw, untagged COSE_Key.
         * @return The parsed [ECPoint] containing the x and y coordinates.
         * @throws IllegalArgumentException if the provided byte array is not a valid COSE key
         * or if the coordinate fields are missing.
         */
        private fun parseEReaderPublicKey(eReaderBytes: ByteArray): ECPoint {
            val cborMapper = CBORMapper()
            val node = cborMapper.readTree(eReaderBytes) as? ObjectNode
                ?: throw IllegalArgumentException("Invalid COSE key")

            val xBytesRaw = node["-2"]?.binaryValue()
            val yBytesRaw = node["-3"]?.binaryValue()

            val xBytes = padEcCoordinatesTo32Bytes(BigInteger(1, xBytesRaw))
            val yBytes = padEcCoordinatesTo32Bytes(BigInteger(1, yBytesRaw))

            val x = BigInteger(1, xBytes)
            val y = BigInteger(1, yBytes)

            return ECPoint(x, y)
        }

        /**
         * Constructs a standard Java [ECPublicKey] from a raw, CBOR-encoded COSE_Key.
         *
         * This function orchestrates the conversion by first parsing the raw COSE key bytes
         * into an [ECPoint] and then using the Java Security Library to generate a [ECPublicKey].
         *
         * @param eReaderBytes The byte array representing the raw, untagged COSE_Key.
         * @return A fully constructed [ECPublicKey] instance.
         */
        fun getEReaderKeyFromParsedCoseKey(eReaderBytes: ByteArray): ECPublicKey {
            val unTaggedBytes = deriveUntaggedCbor(eReaderBytes)
            val parsedKey = parseEReaderPublicKey(unTaggedBytes)
            val params = AlgorithmParameters.getInstance(ELLIPTIC_CURVE_ALGORITHM).apply {
                init(ECGenParameterSpec(ELLIPTIC_CURVE_PARAMETER_SPEC))
            }
            val ecSpec = params.getParameterSpec(java.security.spec.ECParameterSpec::class.java)
            val pubSpec = ECPublicKeySpec(parsedKey, ecSpec)
            return KeyFactory.getInstance(
                ELLIPTIC_CURVE_ALGORITHM
            ).generatePublic(pubSpec) as ECPublicKey
        }
    }
}
