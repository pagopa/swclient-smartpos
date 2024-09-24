package it.pagopa.swc_smartpos.network.errors

import it.pagopa.swc_smartpos.network.BaseNetwork
import it.pagopa.swc_smartpos.network.BaseNetworkTestFactory
import it.pagopa.swc_smartpos.network.UserJson
import it.pagopa.swc_smartpos.network.annotations.Get
import it.pagopa.swc_smartpos.network.annotations.RuntimeUrl
import it.pagopa.swc_smartpos.network.coroutines.utils.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert
import org.junit.Test

class InternalServerError : BaseNetworkTestFactory() {
    @Test
    @Get("change")
    fun tryNetworkError() {
        setSdkInt(23)
        val message = "Internal server error"
        prepareNetwork(wifi = false, cellular = true, ethernet = true)
        val url = mockServer.url("/").toString()
        mockServer.enqueue(
            MockResponse().setResponseCode(500).setBody(message)
        )
        Thread.sleep(1000L)
        val network = BaseNetwork<Array<UserJson>>(scope = CoroutineScope(Dispatchers.IO), this.javaClass.name)
        network.call(
            ctx!!,
            Array<UserJson>::class.java,
            runtimeUrl = arrayOf(RuntimeUrl("change", url))
        )
        network.data.observe(provideLifecycleOwner()) {
            when (it.status) {
                Status.LOADING -> {
                }

                Status.ERROR -> {
                    Assert.assertEquals(it.message == message, true)
                    Assert.assertEquals(it.code == 500, true)
                }

                Status.SUCCESS -> {
                    Assert.assertEquals(it.data is Array<UserJson>, true)
                }
            }
        }
    }
}