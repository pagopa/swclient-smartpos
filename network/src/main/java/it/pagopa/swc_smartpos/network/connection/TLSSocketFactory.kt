package it.pagopa.swc_smartpos.network.connection

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import it.pagopa.swc_smartpos.network.MyLibBuildConfig
import it.pagopa.swc_smartpos.network.R
import it.pagopa.swc_smartpos.network.coroutines.utils.NetworkLogger
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**Socket factory*/
class TLSSocketFactory @Throws(
    KeyManagementException::class,
    NoSuchAlgorithmException::class
) constructor() : SSLSocketFactory() {
    var context: Context? = null
    var trustAllCerts: Array<TrustManager>? = null

    constructor(context: Context, unpinCertificates: Boolean = false) : this() {
        this.context = context

        try {

            // Create an SSLContext that uses our TrustManager
            val sslContext = provideSSlContext()

            if (unpinCertificates) {

                trustAllCerts = arrayOf(@SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }
                })

                sslContext.init(null, trustAllCerts, null)

            } else {

                val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
                val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
                val keyStoreType = KeyStore.getDefaultType()
                val keyStore = KeyStore.getInstance(keyStoreType)
                keyStore.load(null, null)
                val certificate = //if (BuildConfig.BUILD_TYPE == "debug")
                    readCert(this.context!!, R.raw.pago_pa_develop)
                //  else
                //  readCert(this.context!!, R.raw.pago_pa_develop)
                val certificateUat = readCert(this.context!!, R.raw.mil_uat)
                keyStore.setCertificateEntry("ca_l1k0", certificate)
                keyStore.setCertificateEntry("ca_l1k1", certificateUat)
                tmf.init(keyStore)
                sslContext.init(null, tmf.trustManagers, null)
            }

            internalSSLSocketFactory = sslContext.socketFactory

        } catch (e: Exception) {
            e.localizedMessage?.let { NetworkLogger.e(TLSSocketFactory::class.java.name, it) }
        }
    }

    private fun provideSSlContext() =
        if (MyLibBuildConfig.getVersionSDKInt() >= Build.VERSION_CODES.KITKAT_WATCH) {
            SSLContext.getInstance("TLSv1.2")
        } else
            SSLContext.getInstance("TLS")

    private var internalSSLSocketFactory: SSLSocketFactory? = null


    override fun getDefaultCipherSuites(): Array<String> {
        return internalSSLSocketFactory!!.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return internalSSLSocketFactory!!.supportedCipherSuites
    }

    @Throws(IOException::class)
    override fun createSocket(): Socket? {
        return enableTLSOnSocket(internalSSLSocketFactory?.createSocket())
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket? {
        return enableTLSOnSocket(internalSSLSocketFactory?.createSocket(s, host, port, autoClose))
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int): Socket? {
        return enableTLSOnSocket(internalSSLSocketFactory?.createSocket(host, port))
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(
        host: String,
        port: Int,
        localHost: InetAddress,
        localPort: Int
    ): Socket? {
        return enableTLSOnSocket(
            internalSSLSocketFactory?.createSocket(
                host,
                port,
                localHost,
                localPort
            )
        )
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket? {
        return enableTLSOnSocket(internalSSLSocketFactory?.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(
        address: InetAddress,
        port: Int,
        localAddress: InetAddress,
        localPort: Int
    ): Socket? {
        return enableTLSOnSocket(
            internalSSLSocketFactory?.createSocket(
                address,
                port,
                localAddress,
                localPort
            )
        )
    }

    private fun enableTLSOnSocket(socket: Socket?): Socket? {
        if (socket != null && socket is SSLSocket) {
            socket.enabledProtocols = arrayOf("TLSv1.2")
        }
        return socket
    }

    @Throws(CertificateException::class, IOException::class)
    private fun readCert(context: Context, certResourceId: Int): Certificate {

        // read certificate resource
        val caInput = context.resources.openRawResource(certResourceId)
        val ca: Certificate
        caInput.use {
            val cf = CertificateFactory.getInstance("X.509")
            ca = cf.generateCertificate(caInput)
        }
        return ca
    }
}