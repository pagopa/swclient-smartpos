package it.pagopa.swc_smartpos.network.connection

import android.content.Context
import com.google.gson.Gson
import it.pagopa.swc_smartpos.network.BuildConfig
import it.pagopa.swc_smartpos.network.EmptyResponse
import it.pagopa.swc_smartpos.network.connection.Connection.Companion.withUrl
import it.pagopa.swc_smartpos.network.coroutines.utils.NetworkLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import javax.net.ssl.HttpsURLConnection

/**Class to open a connection
 * @see[withUrl]*/
class Connection private constructor() {
    enum class HttpMethod {
        GET, POST, PUT, DELETE, PATCH
    }

    private data class Timeouts(var short: Long = 30_000, var long: Long = 60_000) :
        java.io.Serializable

    private val tag = "Connection"
    private lateinit var url: String
    var method: HttpMethod = HttpMethod.GET
        private set
    private var timeouts = Timeouts()
    private var customUrlConnection = CustomUrlConnection()
    private var payloads = Payloads()

    /**HttpMethod to use
     * @see[HttpMethod]*/
    fun withHttpMethod(method: HttpMethod) = apply {
        this@apply.method = method
    }

    /**Timeouts to use
     * @see[Timeouts]*/
    fun withTimeouts(short: Long, long: Long) = apply {
        this@apply.timeouts.short = short
        this@apply.timeouts.long = long
    }

    /**custom params to use for connection
     * @see[CustomUrlConnection]*/
    fun withUrlConnection(urlConnection: CustomUrlConnection) = apply {
        this.customUrlConnection = urlConnection
    }

    /**Payloads to send
     * @param[body] body payload
     * @see[Payloads]*/
    fun withPayloads(body: Any) = apply {
        this.payloads.bodyPayload = body
    }

    /**Class to customize your request*/
    data class CustomUrlConnection(
        val contentType: String = "application/json;charset=UTF-8",
        val contentLanguage: String = "en-US",
        val acceptEncoding: String = "gzip",
        val userAgent: String? = null,
        val bearer: String? = null,
        val useShortTimeout: Boolean = true,
        val followRedirects: Boolean = false,
        val customHeader: Map<String, String>? = null
    ) : java.io.Serializable

    data class Payloads(
        var headerPayload: Any? = null,
        var bodyPayload: Any? = null
    ) : java.io.Serializable

    /**f to open the connection to the url provided in the constructor [withUrl]
     * @param[context] your context
     * @param[action] lambda expression to manage the call
     * @see[Response]*/
    suspend fun openConnection(
        context: Context,
        action: (Response) -> Unit
    ) {
        if (ConnectionHelper(context).isConnectionAvailable()) {
            NetworkLogger.i("opening connection to", this@Connection.url)
            withContext(Dispatchers.IO) {
                val url = URL(this@Connection.url)
                // #1 opens connection
                val conn = UrlConnection(url.openConnection())
                // #2 sets headers (and 'Cookie')
                conn.withContentType(this@Connection.customUrlConnection.contentType)
                conn.withContentLanguage(this@Connection.customUrlConnection.contentLanguage)
                conn.withAcceptEncoding(this@Connection.customUrlConnection.acceptEncoding)
                this@Connection.customUrlConnection.userAgent?.let { conn.withUserAgent(it) }
                this@Connection.customUrlConnection.bearer?.let { conn.withBearer(it) }
                this@Connection.customUrlConnection.customHeader?.let {
                    conn.withCustomHeader(it)
                    NetworkLogger.i("custom header", it.toString())
                }
                NetworkLogger.i(
                    "customUrlConnection",
                    this@Connection.customUrlConnection.toString()
                )
                // #3 sets connection properties
                val urlConnection = conn.url
                if (urlConnection !is HttpURLConnection) {
                    throw NoHttpCallException()
                }
                val timeout = if (this@Connection.customUrlConnection.useShortTimeout)
                    this@Connection.timeouts.short.toInt()
                else
                    this@Connection.timeouts.long.toInt()
                urlConnection.readTimeout = timeout
                urlConnection.connectTimeout = timeout
                urlConnection.doInput = true
                urlConnection.requestMethod = this@Connection.method.toString()
                urlConnection.instanceFollowRedirects =
                    this@Connection.customUrlConnection.followRedirects
                // #3.1 in case of https sets socket factory
                val tlsSF = TLSSocketFactory(context, !BuildConfig.FLAVOR.equals("prod", true))
                (urlConnection as? HttpsURLConnection)?.sslSocketFactory = tlsSF
                // #3.2 in case POST, writes payload
                if (this@Connection.method != HttpMethod.GET) {
                    urlConnection.doOutput = true
                    val payload = payloads.bodyPayload.toString()
                    NetworkLogger.i("payload", payload)
                    val bytes = payload.toByteArray(StandardCharsets.UTF_8)
                    val os = urlConnection.outputStream
                    os.write(bytes)
                    os.flush()
                    os.close()
                }
                // #4 handles response
                val itsOk: Boolean
                val code = urlConnection.responseCode
                var br: BufferedReader? = null
                when (code) {
                    HttpsURLConnection.HTTP_MOVED_TEMP, HttpsURLConnection.HTTP_OK, HttpsURLConnection.HTTP_CREATED,
                    HttpsURLConnection.HTTP_ACCEPTED, HttpsURLConnection.HTTP_NOT_AUTHORITATIVE -> {
                        itsOk = true
                        br =
                            if (urlConnection.headerFields.containsKey(contentEncoding) && urlConnection.getHeaderField(
                                    contentEncoding
                                )
                                    .equals("gzip", true)
                            ) {
                                BufferedReader(GZIPInputStream(urlConnection.inputStream).reader())
                            } else
                                BufferedReader(InputStreamReader(urlConnection.inputStream))
                    }

                    HttpsURLConnection.HTTP_NO_CONTENT -> itsOk = true
                    else -> {
                        itsOk = false
                        br =
                            if (urlConnection.headerFields.containsKey(contentEncoding) && urlConnection.getHeaderField(
                                    contentEncoding
                                ).equals("gzip", true)
                            ) {
                                BufferedReader(GZIPInputStream(urlConnection.errorStream).reader())
                            } else {
                                BufferedReader(InputStreamReader(urlConnection.errorStream))
                            }
                    }
                }
                val myResponse = br?.readText()
                br?.close()
                NetworkLogger.i(tag, code.toString())
                NetworkLogger.i(tag, myResponse.toString())
                action.invoke(
                    Response(
                        if (itsOk) KindOfResponse.Ok
                        else
                            KindOfResponse.Fail,
                        Gson().toJson(urlConnection.headerFields),
                        if (myResponse == null || myResponse == "")
                            Gson().toJson(EmptyResponse(""))
                        else myResponse,
                        code
                    )
                )
            }
        } else {
            NetworkLogger.e(tag, "No network active!!")
            action.invoke(Response(KindOfResponse.NoNetwork))
        }
    }

    data class Response(
        val kindOfResponse: KindOfResponse,
        val responseHeaders: String = "",
        val response: String = "",
        val code: Int? = null
    ) : java.io.Serializable

    class NoHttpCallException : Throwable("No Http")
    enum class KindOfResponse {
        NoNetwork,
        Ok,
        Fail
    }

    companion object {
        /**Url used by client*/
        fun withUrl(url: String) = Connection().apply {
            this@apply.url = "${BuildConfig.API_URL}$url"
        }

        private const val contentEncoding = "Content-Encoding"
    }
}