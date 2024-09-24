package it.pagopa.swc_smartpos.network

import android.content.Context
import android.content.res.Resources
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import it.pagopa.swc_smartpos.network.connection.TLSSocketFactory
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.security.Principal
import java.security.Provider
import java.security.PublicKey
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.X509TrustManager

class TlsSocketFactoryTest {
    private var ctx: Context? = null
    private var res: Resources? = null

    @Before
    fun before() {
        ctx = mockk()
        res = mockk()
    }

    @Test
    fun tlsSocketFactoryTest() {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        val file = File("../network/src/main/res/raw/pago_pa_develop.cer")
        every { res!!.openRawResource(R.raw.pago_pa_develop) } returns FileInputStream(file)
        every { ctx!!.resources } returns res!!
        val tls = TLSSocketFactory()
        assert(tls.context == null)
        val tlsInitialized = TLSSocketFactory(ctx!!)
        assert(tlsInitialized.context == ctx)
        assert(tlsInitialized.trustAllCerts == null)
        val tlsInitializedTwo = TLSSocketFactory(ctx!!, true)
        assert(tlsInitializedTwo.trustAllCerts != null)
        tlsInitializedTwo.defaultCipherSuites
        tlsInitializedTwo.supportedCipherSuites
        val x509TrustManager = tlsInitializedTwo.trustAllCerts!![0] as X509TrustManager
        x509TrustManager.checkClientTrusted(arrayOf(FakeCertificate()), "ciao")
        x509TrustManager.checkServerTrusted(arrayOf(FakeCertificate()), "ciao")
        assert(x509TrustManager.acceptedIssuers is Array<X509Certificate>)
    }

    private class FakeCertificate : X509Certificate() {
        override fun checkValidity() {}
        override fun checkValidity(date: Date?) {}
        override fun getVersion(): Int = 1
        override fun toString(): String = this.javaClass.name
        override fun verify(key: PublicKey?) {}
        override fun verify(key: PublicKey?, sigProvider: String?) {}
        override fun getEncoded(): ByteArray = byteArrayOf(Byte.MIN_VALUE)
        override fun hasUnsupportedCriticalExtension(): Boolean = false
        override fun getCriticalExtensionOIDs(): MutableSet<String> = ArrayList<String>().apply { add("ciao") }.toMutableSet()
        override fun getNonCriticalExtensionOIDs(): MutableSet<String> = ArrayList<String>().apply { add("test") }.toMutableSet()
        override fun getExtensionValue(oid: String?): ByteArray = this@FakeCertificate.encoded
        override fun getNotBefore(): Date = Date()
        override fun getNotAfter(): Date = Date()
        override fun getSerialNumber(): BigInteger = BigInteger.ONE
        override fun getIssuerDN(): Principal = Principal { "test" }
        override fun getSubjectDN(): Principal = Principal { "test" }
        override fun getTBSCertificate(): ByteArray = this.encoded
        override fun getSignature(): ByteArray = this.encoded
        override fun getSigAlgName(): String = "Test"
        override fun getSigAlgOID(): String = "Test"
        override fun getSigAlgParams(): ByteArray = this.encoded
        override fun getIssuerUniqueID(): BooleanArray = BooleanArray(2) { it == 0 }
        override fun getSubjectUniqueID(): BooleanArray = BooleanArray(2) { it == 0 }
        override fun getKeyUsage(): BooleanArray = BooleanArray(2) { it == 0 }
        override fun getBasicConstraints(): Int = 0
        override fun verify(key: PublicKey?, sigProvider: Provider?) {
            super.verify(key, sigProvider)
        }

        override fun getPublicKey(): PublicKey {
            return object : PublicKey {
                override fun getAlgorithm(): String = "ciao"
                override fun getFormat(): String = "test"
                override fun getEncoded(): ByteArray = this@FakeCertificate.encoded
            }
        }
    }
}