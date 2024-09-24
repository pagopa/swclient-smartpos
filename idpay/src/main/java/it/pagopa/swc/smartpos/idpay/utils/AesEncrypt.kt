package it.pagopa.swc.smartpos.idpay.utils

import android.os.Build
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import it.pagopa.readcie.nfc.Utils
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import java.security.interfaces.RSAPublicKey
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**Instantiate this class once per time else secretKey will be re-instantiated*/
class AesEncrypt {
    data class EncryptionException(override val message: String) : Exception()

    @get:RequiresApi(Build.VERSION_CODES.M)
    @delegate:RequiresApi(Build.VERSION_CODES.M)
    private val secretKey by lazy {
        AesEncrypting().generateAesKey()
    }

    inner class PinBlock(private val secondFactor: String) {
        private fun fromHex(c: Char): Int {
            if (c in '0'..'9')
                return c.code - '0'.code
            if (c in 'A'..'F')
                return c.code - 'A'.code + 10
            if (c in 'a'..'f')
                return c.code - 'a'.code + 10
            throw IllegalArgumentException()
        }

        private fun toHex(nybble: Int): Char {
            require(!(nybble < 0 || nybble > 15))
            return "0123456789ABCDEF"[nybble]
        }

        private infix fun String.xorHex(b: String): String {
            val chars = CharArray(this.length)
            for (i in chars.indices)
                chars[i] = toHex(fromHex(this[i]) xor fromHex(b[i]))
            return String(chars).uppercase(Locale.getDefault())
        }

        fun generate(pin: String): Pair<Boolean, String> {
            var pinBlock = String.format("%s%d%s", "0", pin.length, pin)
            while (pinBlock.length != 16) {
                pinBlock += "F"
            }
            return try {
                val generated = pinBlock xorHex secondFactor
                true to generated
            } catch (e: Exception) {
                if (e is java.lang.IllegalArgumentException)
                    false to e.message.orEmpty()//SecondFactor Not valid
                else
                    false to e.message.orEmpty()
            }
        }
    }

    inner class RsaWithSameSecretKey {
        @Throws(EncryptionException::class)
        fun encryptSessionKeyWithRsa(rsaPublicKey: RSAPublicKey): String {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) throw EncryptionException("devices before MarshMallow have not AES algorithm")
            val aesKey = secretKey ?: throw EncryptionException("fail to generate aesKey")
            val cipher = Cipher.getInstance("RSA/ECB/OAEPwithSHA-256andMGF1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey)
            return Base64.encodeToString(cipher.doFinal(aesKey.encoded), Base64.NO_WRAP or Base64.DEFAULT)
        }
    }

    @Throws(EncryptionException::class)
    fun encrypt(pin: String): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) throw EncryptionException("devices before MarshMallow have not AES algorithm")
        val aesKey = secretKey ?: throw EncryptionException("fail to generate aesKey")
        val encryptedByte = AesEncrypting().encryptData(aesKey.encoded, Utils.hexStringToByteArray(pin)) ?: throw EncryptionException("fail to encrypt data")
        return Base64.encodeToString(encryptedByte, Base64.NO_WRAP or Base64.DEFAULT)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private class AesEncrypting {
        private val tag = "AesEncrypting"
        private val transformation = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"

        fun generateAesKey(): SecretKey? {
            return try {
                val instance = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
                instance.init(256)
                return instance.generateKey()
            } catch (e: Exception) {
                WrapperLogger.e(tag, e.toString())
                null
            }
        }

        fun encryptData(aesKeyEncoded: ByteArray, plainText: ByteArray): ByteArray? {
            return try {
                val cipher = Cipher.getInstance(transformation)
                val zeroByte = 0.toByte()//TODO change IvParameterSpec with a shared IV
                cipher.init(
                    Cipher.ENCRYPT_MODE, SecretKeySpec(aesKeyEncoded, KeyProperties.KEY_ALGORITHM_AES), IvParameterSpec(
                        byteArrayOf(
                            zeroByte, zeroByte, zeroByte, zeroByte, zeroByte, zeroByte, zeroByte, zeroByte,
                            zeroByte, zeroByte, zeroByte, zeroByte, zeroByte, zeroByte, zeroByte, zeroByte,
                        )
                    )
                )
                cipher.doFinal(plainText)
            } catch (e: Exception) {
                WrapperLogger.e(tag, e.toString())
                null
            }
        }

        /*fun decrypt(aesKeyEncoded: ByteArray, encrypted: ByteArray): String {
            val cipher = Cipher.getInstance(transformation)
            val zeroByte = 0.toByte()
            cipher.init(
                Cipher.DECRYPT_MODE, SecretKeySpec(aesKeyEncoded, KeyProperties.KEY_ALGORITHM_AES), IvParameterSpec(
                    byteArrayOf(
                        zeroByte, zeroByte, zeroByte, zeroByte, zeroByte, zeroByte, zeroByte, zeroByte,
                        zeroByte, zeroByte, zeroByte, zeroByte, zeroByte, zeroByte, zeroByte, zeroByte,
                    )
                )
            )
            return AppUtil.bytesToHex(cipher.doFinal(encrypted))
        }*/
    }
}
