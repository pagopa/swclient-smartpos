package it.pagopa.swc_smartpos.network

import io.mockk.every
import it.pagopa.swc_smartpos.network.connection.Connection
import it.pagopa.swc_smartpos.network.connection.ConnectionHelper
import it.pagopa.swc_smartpos.network.connection.UrlConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Test
import java.net.URL

class GenericTests : BaseNetworkTestFactory() {
    @Test
    fun connectionTest() {
        val url = "https://jsonplaceholder.typicode.com/posts"
        val connection = Connection.withUrl(url)
        connection.withHttpMethod(Connection.HttpMethod.POST)
        connection.withUrlConnection(Connection.CustomUrlConnection())
        connection.withPayloads(Any())
        connection.withTimeouts(3000L, 60000L)
        CoroutineScope(Dispatchers.IO).launch {
            connection.openConnection(
                ctx!!
            ) {
                assert(it.code == 200)
            }
        }
        assert(connection.method == Connection.HttpMethod.POST)
    }

    @Test
    fun helperTest() {
        every { ctx!!.getSystemService(any()) } returns null
        setSdkInt(23)
        assert(!ConnectionHelper(ctx!!).isConnectionAvailable())
        setSdkInt(19)
        assert(!ConnectionHelper(ctx!!).isConnectionAvailable())
    }

    @Test
    fun urlConnectionTest() {
        prepareNetwork()
        val urlFake = "https://jsonplaceholder.typicode.com/posts"
        val url = URL(urlFake)
        val urlConn = UrlConnection(url.openConnection())
        urlConn.withBearer("Bearer myBearer")
        urlConn.withUserAgent("Agent")
        urlConn.withContentType("application/json; charset=UTF-8")
        urlConn.withAcceptEncoding("gzip")
        urlConn.withContentLanguage("en-US")
        assert(urlConn.url.contentEncoding == "gzip")
        assert(urlConn.url.contentType == "application/json; charset=utf-8")
    }
}