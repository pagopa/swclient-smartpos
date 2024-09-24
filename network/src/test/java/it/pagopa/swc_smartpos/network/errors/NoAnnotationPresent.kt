package it.pagopa.swc_smartpos.network.errors

import com.google.gson.Gson
import it.pagopa.swc_smartpos.network.BaseNetwork
import it.pagopa.swc_smartpos.network.BaseNetworkTestFactory
import it.pagopa.swc_smartpos.network.OtherUserJson
import it.pagopa.swc_smartpos.network.UserJson
import it.pagopa.swc_smartpos.network.annotations.RuntimeUrl
import it.pagopa.swc_smartpos.network.connection.Connection
import it.pagopa.swc_smartpos.network.coroutines.utils.Resource
import it.pagopa.swc_smartpos.network.coroutines.utils.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.mockwebserver.MockResponse
import org.junit.Test

class NoAnnotationPresent : BaseNetworkTestFactory() {
    @Test
    fun onlyTestAnnotation() {
        setSdkInt(23)
        prepareNetwork()
        val url = mockServer.url("/").toString()
        mockServer.enqueue(
            MockResponse()
                .setBody(Gson().toJson(arrayOf(UserJson(1, 12, "ciao", "ciao"))))
        )
        val network = BaseNetwork<Any>(scope = CoroutineScope(Dispatchers.IO), this.javaClass.name)
        network.call(
            ctx!!,
            OtherUserJson("ciao", "test"),
            OtherUserJson::class.java,
            Any::class.java,
            custom = Connection.CustomUrlConnection(customHeader = mapOf("ciao" to "ciao")),
            runtimeUrl = arrayOf(RuntimeUrl("change", url))
        )
        Thread.sleep(1000L)
        network.data.observe(provideLifecycleOwner()) {
            when (it.status) {
                Status.LOADING -> {
                }

                Status.ERROR -> assert(it.message?.contains("No annotation present to call this class", true) == true)
                Status.SUCCESS -> {}
            }
        }
    }

    @Test
    fun onlyTestAnnotation2() {
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
                Status.LOADING -> {}
                Status.ERROR -> assert(it.message?.contains("No annotation present to call this class", true) == true)
                Status.SUCCESS -> {}
            }
        }
    }
}