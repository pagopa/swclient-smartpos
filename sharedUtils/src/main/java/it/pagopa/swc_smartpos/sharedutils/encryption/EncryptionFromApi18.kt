@file:Suppress("DEPRECATION")

package it.pagopa.swc_smartpos.sharedutils.encryption

import android.content.Context
import android.security.KeyPairGeneratorSpec
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Calendar
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.security.auth.x500.X500Principal


class EncryptionFromApi18 {
    private val loggerName = "keyStoreFail"
    private fun KeyStore.createNewKeysWith(context: Context, alias: String): String {
        try {
            if (!this.containsAlias(alias)) {
                val start: Calendar = Calendar.getInstance()
                val end: Calendar = Calendar.getInstance()
                end.add(Calendar.YEAR, 1)
                val spec = KeyPairGeneratorSpec.Builder(context)
                    .setAlias(alias)
                    .setSubject(X500Principal("CN=Sample Name, O=Android Authority"))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(start.time)
                    .setEndDate(end.time)
                    .build()
                val generator: KeyPairGenerator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
                generator.initialize(spec)
                generator.generateKeyPair()
            }
        } catch (e: Exception) {
            WrapperLogger.e(loggerName, e.toString())
        }
        return alias
    }

    fun encrypt(context: Context, text: String?, keyStore: KeyStore): String? {
        if (text.isNullOrEmpty()) {
            return text
        }
        try {
            val privateKeyEntry: KeyStore.PrivateKeyEntry? = keyStore.getEntry(keyStore.createNewKeysWith(context, alias), null) as? KeyStore.PrivateKeyEntry
            val publicKey: PublicKey? = privateKeyEntry?.certificate?.publicKey
            val inCipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey)
            val outputStream = ByteArrayOutputStream()
            val cipherOutputStream = CipherOutputStream(
                outputStream, inCipher
            )
            cipherOutputStream.write(text.toByteArray(charset("UTF-8")))
            cipherOutputStream.close()
            return android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            WrapperLogger.e(loggerName, e.toString())
        }
        return text
    }

    fun decrypt(text: String?, keyStore: KeyStore): String? {
        if (text.isNullOrEmpty()) {
            return text
        }
        try {
            val privateKeyEntry: KeyStore.PrivateKeyEntry? = keyStore.getEntry(alias, null) as? KeyStore.PrivateKeyEntry
            val privateKey: PrivateKey? = privateKeyEntry?.privateKey
            val output: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            output.init(Cipher.DECRYPT_MODE, privateKey)
            val cipherInputStream = CipherInputStream(
                ByteArrayInputStream(android.util.Base64.decode(text, android.util.Base64.DEFAULT)), output
            )
            val values: ArrayList<Byte> = ArrayList()
            var nextByte: Int
            while (cipherInputStream.read().also { nextByte = it } != -1) {
                values.add(nextByte.toByte())
            }
            val bytes = ByteArray(values.size)
            for (i in bytes.indices) {
                bytes[i] = values[i]
            }
            return String(bytes, 0, bytes.size, Charsets.UTF_8)
        } catch (e: Exception) {
            WrapperLogger.e(loggerName, e.toString())
        }
        return text
    }

    companion object {
        private const val alias = "aliasSwc2"
    }
}