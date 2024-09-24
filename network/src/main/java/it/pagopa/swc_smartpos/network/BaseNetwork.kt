package it.pagopa.swc_smartpos.network

import android.content.Context
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import it.pagopa.swc_smartpos.network.annotations.*
import it.pagopa.swc_smartpos.network.connection.Connection
import it.pagopa.swc_smartpos.network.coroutines.utils.NetworkLogger
import it.pagopa.swc_smartpos.network.coroutines.utils.Resource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.UnsupportedEncodingException
import java.lang.reflect.Method
import java.lang.reflect.Type
import java.net.URLEncoder

/**Class to implement a call and receive back a LiveData Value [data]*/
class BaseNetwork<T>(private val scope: CoroutineScope, private val className: String) {
    private val _data = MutableLiveData<Resource<T>>()
    val data: LiveData<Resource<T>> = _data
    private var mUrl: String? = null
    private var mMethod: Connection.HttpMethod = Connection.HttpMethod.GET
    private var isNotIntoStackTrace = false

    init {
        val previousStackTrace = Thread.currentThread().stackTrace.find { it.className == className }
        if (previousStackTrace == null) {
            isNotIntoStackTrace = true
        } else {
            val callerMethodName = previousStackTrace.methodName
            val clazz = Class.forName(className)
            var method: Method? = null
            clazz.methods.forEach {
                if (it.name == callerMethodName)
                    method = it
            }
            method?.annotations?.forEach {
                when (it) {
                    is Get -> {
                        mMethod = Connection.HttpMethod.GET
                        mUrl = it.url
                    }

                    is Post -> {
                        mMethod = Connection.HttpMethod.POST
                        mUrl = it.url
                    }

                    is Patch -> {
                        mMethod = Connection.HttpMethod.PATCH
                        mUrl = it.url
                    }

                    is Put -> {
                        mMethod = Connection.HttpMethod.PUT
                        mUrl = it.url
                    }

                    is Delete -> {
                        mMethod = Connection.HttpMethod.DELETE
                        mUrl = it.url
                    }
                }
            }
        }
    }

    private sealed class CoroutineEx : CancellationException() {
        data class NoAnnotationPresent(override val message: String = "No annotation present to call this class") : CoroutineEx()
        data class NotIntoStackTrace(override val message: String = "Class not present into stack trace!!") : CoroutineEx()
    }

    private fun <Payload> Payload?.provideConnection(custom: Connection.CustomUrlConnection?, payloadType: Type?, disableHtmlEscaping: Boolean): Connection? {
        return if (mUrl == null) null else {
            val conn = Connection.withUrl(mUrl!!)
                .withHttpMethod(mMethod)
                .withUrlConnection(custom ?: Connection.CustomUrlConnection("application/json;charset=UTF-8"))
            this?.let {
                conn.withPayloads(
                    if (custom?.contentType == "application/x-www-form-urlencoded")
                        this.asUrlEncoded(payloadType)
                    else {
                        if (disableHtmlEscaping) {
                            val gson = GsonBuilder().disableHtmlEscaping().create()
                            gson.toJson(it, payloadType)
                        } else
                            Gson().toJson(it, payloadType)
                    }
                )
            }
            conn
        }
    }

    private fun String.doAsUrlEncoded(): String {
        val sb = StringBuilder()
        val string = this.replace("{", "").replace("}", "")
        val new = string.replace(":", "=").replace(",", "&").replace("\"", "")
        val sequence = new.splitToSequence("=")
        sequence.forEach {
            if (it.contains("&")) {
                sb.append(URLEncoder.encode(it.split("&")[0], Charsets.UTF_8.name()))
                sb.append("&")
                sb.append(URLEncoder.encode(it.split("&")[1], Charsets.UTF_8.name()))
            } else
                sb.append(URLEncoder.encode(it, Charsets.UTF_8.name()))
            if (it != sequence.last())
                sb.append("=")
        }
        return sb.toString()
    }

    @Throws(UnsupportedEncodingException::class)
    private fun <Payload> Payload?.asUrlEncoded(payloadType: Type?): String {
        if (this == null) return ""
        return Gson().toJson(this, payloadType).doAsUrlEncoded()
    }

    private fun provideConnection(custom: Connection.CustomUrlConnection?): Connection? {
        return if (mUrl == null) null else {
            return Connection.withUrl(mUrl!!)
                .withHttpMethod(mMethod)
                .withUrlConnection(custom ?: Connection.CustomUrlConnection("application/json;charset=UTF-8"))
        }
    }

    private fun Connection.Response.parse(typeToken: Type, addHeader: Boolean = false) {
        when (this.kindOfResponse) {
            Connection.KindOfResponse.NoNetwork -> _data.postValue(Resource.error(null, "no network available"))
            Connection.KindOfResponse.Fail -> _data.postValue(Resource.error(this.code, this.response))
            Connection.KindOfResponse.Ok -> {
                runCatching {
                    val toParse = if (addHeader) {
                        "{${
                            this.responseHeaders
                                .replaceFirst("{", "")
                                .replaceLast("}", "")
                        },${
                            this.response.replaceFirst("{", "")
                                .replaceLast("}", "")
                        }}"
                    } else
                        this.response
                    NetworkLogger.d("toParse", toParse)
                    Gson().fromJson<T>(toParse, typeToken)
                }.onSuccess {
                    _data.postValue(Resource.success(this.code, it))
                    NetworkLogger.i("Response ok", it.toString())
                }.onFailure {
                    _data.postValue(Resource.error(this.code, it.toString()))
                    NetworkLogger.e("Response failure", it.toString())
                }
            }
        }
    }

    private fun String.replaceLast(substring: String, replacement: String): String {
        val index = this.lastIndexOf(substring)
        return if (index == -1) this else (this.substring(0, index) + replacement
                + this.substring(index + substring.length))
    }

    private fun Array<RuntimeUrl>?.changeUrl() {
        this?.let { runtimeUrlObj ->
            runtimeUrlObj.forEach {
                mUrl = mUrl?.replace(
                    it.oldValue, when {
                        it.isPath -> URLEncoder.encode(it.newValue, Charsets.UTF_8.name())
                        it.isBase64 -> Base64.encodeToString(it.newValue.toByteArray(), Base64.DEFAULT).replace("=", "")
                        else -> it.newValue
                    }
                )
            }
        }
    }

    fun call(
        context: Context,
        typeToken: Type,
        custom: Connection.CustomUrlConnection? = null,
        runtimeUrl: Array<RuntimeUrl>? = null,
        addHeader: Boolean = false
    ) {
        runtimeUrl.changeUrl()
        _data.postValue(Resource.loading())
        scope.launch {
            try {
                if (isNotIntoStackTrace) throw CoroutineEx.NotIntoStackTrace()
                val connection = provideConnection(custom) ?: throw CoroutineEx.NoAnnotationPresent()
                connection.openConnection(context) { response ->
                    response.parse(typeToken, addHeader)
                }
            } catch (e: Exception) {
                _data.postValue(Resource.error(0, e.toString()))
                NetworkLogger.e("Response ex.", e.toString())
            }
        }
    }

    fun <Payload> call(
        context: Context,
        payload: Payload? = null,
        payloadType: Type? = null,
        typeToken: Type,
        custom: Connection.CustomUrlConnection? = null,
        runtimeUrl: Array<RuntimeUrl>? = null,
        addHeader: Boolean = false,
        disableHtmlEscaping: Boolean = false
    ) {
        runtimeUrl.changeUrl()
        _data.postValue(Resource.loading())
        scope.launch {
            try {
                if (isNotIntoStackTrace) throw CoroutineEx.NotIntoStackTrace()
                val connection = payload.provideConnection(custom, payloadType, disableHtmlEscaping) ?: throw CoroutineEx.NoAnnotationPresent()
                connection.openConnection(context) { response ->
                    response.parse(typeToken, addHeader)
                }
            } catch (e: Exception) {
                _data.postValue(Resource.error(0, e.toString()))
                NetworkLogger.e("Response ex.", e.toString())
            }
        }
    }
}
