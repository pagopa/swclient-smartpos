package it.pagopa.swc_smartpos.network.clients

import com.google.gson.Gson
import it.pagopa.swc_smartpos.network.BaseNetwork
import it.pagopa.swc_smartpos.network.BaseNetworkTestFactory
import it.pagopa.swc_smartpos.network.OtherUserJson
import it.pagopa.swc_smartpos.network.UserJson
import it.pagopa.swc_smartpos.network.annotations.Post
import it.pagopa.swc_smartpos.network.annotations.RuntimeUrl
import it.pagopa.swc_smartpos.network.connection.Connection
import it.pagopa.swc_smartpos.network.coroutines.utils.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class PostClientTest : BaseNetworkTestFactory() {
    @Test
    @Post("change")
    fun postCall() {
        setSdkInt(23)
        prepareNetwork()
        val url = mockServer.url("/").toString()
        mockServer.enqueue(
            MockResponse()
                .setBody(Gson().toJson(arrayOf(UserJson(1, 12, "ciao", "ciao"))))
        )
        val network = BaseNetwork<Array<UserJson>>(scope = CoroutineScope(Dispatchers.IO), this.javaClass.name)
        network.call(
            ctx!!,
            OtherUserJson("ciao", "test"),
            OtherUserJson::class.java,
            Array<UserJson>::class.java,
            custom = Connection.CustomUrlConnection(customHeader = mapOf("ciao" to "ciao")),
            runtimeUrl = arrayOf(RuntimeUrl("change", url))
        )
        Thread.sleep(1000L)
        network.data.observe(provideLifecycleOwner()) {
            when (it.status) {
                Status.LOADING -> {
                }

                Status.ERROR -> {
                }

                Status.SUCCESS -> {
                    assertEquals(it.data is Array<UserJson>, true)
                }
            }
        }
    }
}