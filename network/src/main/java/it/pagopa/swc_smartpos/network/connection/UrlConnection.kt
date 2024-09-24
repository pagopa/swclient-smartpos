package it.pagopa.swc_smartpos.network.connection

import java.net.URLConnection

/**Class to manage our url connection*/
internal class UrlConnection(val url: URLConnection) {
    fun withContentType(contentType: String) = apply {
        url.setRequestProperty("Content-Type", contentType)//example:application/json;charset=UTF-8
    }

    fun withContentLanguage(contentLanguage: String) = apply {
        url.setRequestProperty("Content-Language", contentLanguage)//example: en-US
    }

    fun withUserAgent(userAgent: String) = apply {
        url.setRequestProperty("User-Agent", userAgent)
    }

    fun withAcceptEncoding(acceptEncoding: String) = apply {
        url.setRequestProperty("Accept-Encoding", acceptEncoding)//example: gzip
    }

    fun withBearer(bearer: String) = apply {
        url.setRequestProperty("Authorization", "Bearer $bearer")
    }

    fun withCustomHeader(array: Map<String, String>) = apply {
        array.entries.forEach {
            url.setRequestProperty(it.key, it.value)
        }
    }
}