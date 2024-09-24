package it.pagopa.swc_smartpos.network.clients

import com.google.gson.Gson
import it.pagopa.swc_smartpos.network.BaseNetwork
import it.pagopa.swc_smartpos.network.BaseNetworkTestFactory
import it.pagopa.swc_smartpos.network.UserJson
import it.pagopa.swc_smartpos.network.UserJsonForHeader
import it.pagopa.swc_smartpos.network.annotations.Get
import it.pagopa.swc_smartpos.network.annotations.RuntimeUrl
import it.pagopa.swc_smartpos.network.coroutines.utils.Resource
import it.pagopa.swc_smartpos.network.coroutines.utils.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert
import org.junit.Test

class GetClientTest : BaseNetworkTestFactory() {
    @Get("change")
    @Test
    fun tryNetworkSuccess() {
        prepareNetwork(wifi = false, cellular = false, ethernet = true)
        val url = mockServer.url("/").toString()
        mockServer.enqueue(
            MockResponse().setBody(Gson().toJson(arrayOf(UserJson(1, 12, "ciao", "ciao"))))
        )
        val network = BaseNetwork<Array<UserJson>>(scope = CoroutineScope(Dispatchers.IO), this.javaClass.name)
        network.call(
            ctx!!,
            Array<UserJson>::class.java,
            runtimeUrl = arrayOf(RuntimeUrl("change", url))
        )
        assert(network.data.value is Resource)
        Thread.sleep(5000L)
        network.data.observe(provideLifecycleOwner()) {
            when (it.status) {
                Status.LOADING -> assert(it.message == null)
                Status.ERROR -> Assert.assertEquals(it.message != "", true)
                Status.SUCCESS -> {
                    assert(it.data is Array<UserJson>)
                }
            }
        }
    }

    @Get("change")
    @Test
    fun tryNetworkSuccessWithHeaderResponse() {
        prepareNetwork(wifi = true, cellular = false, ethernet = false)
        val url = mockServer.url("/").toString()
        mockServer.enqueue(
            MockResponse().setHeader("headerVal", listOf("valueMocked")).setBody("{\"ciao\":\"hello\",\"bye\":\"goodBye\"}")
        )
        val network = BaseNetwork<UserJsonForHeader>(scope = CoroutineScope(Dispatchers.IO), this.javaClass.name)
        network.call(
            ctx!!,
            UserJsonForHeader::class.java,
            runtimeUrl = arrayOf(RuntimeUrl("change", url)),
            addHeader = true
        )
        assert(network.data.value is Resource)
        Thread.sleep(5000L)
        network.data.observe(provideLifecycleOwner()) {
            when (it.status) {
                Status.LOADING -> assert(it.message == null)
                Status.ERROR -> Assert.assertEquals(it.message != "", true)
                Status.SUCCESS -> {
                    assert(it.data is UserJsonForHeader)
                    assert((it.data as UserJsonForHeader).ciao == "hello")
                    assert((it.data as UserJsonForHeader).bye == "goodBye")
                    assert((it.data as UserJsonForHeader).headerVal == "valueMocked")
                }
            }
        }
    }
}