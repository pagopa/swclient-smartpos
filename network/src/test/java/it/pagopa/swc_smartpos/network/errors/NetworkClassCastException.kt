package it.pagopa.swc_smartpos.network.errors

import com.google.gson.Gson
import it.pagopa.swc_smartpos.network.BaseNetwork
import it.pagopa.swc_smartpos.network.BaseNetworkTestFactory
import it.pagopa.swc_smartpos.network.OtherUserJson
import it.pagopa.swc_smartpos.network.UserJson
import it.pagopa.swc_smartpos.network.annotations.Get
import it.pagopa.swc_smartpos.network.annotations.RuntimeUrl
import it.pagopa.swc_smartpos.network.coroutines.utils.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert
import org.junit.Test

class NetworkClassCastException : BaseNetworkTestFactory() {
    @Test
    @Get("change")
    fun tryNetworkClassCastException() {
        prepareNetwork()
        val url = mockServer.url("/").toString()
        mockServer.enqueue(
            MockResponse().setBody(Gson().toJson(OtherUserJson("cc", "bb")))
        )
        val network = BaseNetwork<Array<UserJson>>(scope = CoroutineScope(Dispatchers.IO), this.javaClass.name)
        network.call(
            ctx!!,
            Array<UserJson>::class.java,
            runtimeUrl = arrayOf(RuntimeUrl("change", url))
        )
        Thread.sleep(1000L)
        network.data.observe(provideLifecycleOwner()) {
            when (it.status) {
                Status.LOADING -> {
                }
                Status.ERROR -> Assert.assertEquals(it.message != "", true)
                Status.SUCCESS -> {
                    Assert.assertEquals(it.data is Array<UserJson>, true)
                }
            }
        }
    }
}