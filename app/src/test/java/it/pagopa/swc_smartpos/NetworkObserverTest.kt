package it.pagopa.swc_smartpos

import it.pagopa.swc_smartpos.model.BaseResponse
import it.pagopa.swc_smartpos.network.BaseNetwork
import it.pagopa.swc_smartpos.network.NetworkObserver
import it.pagopa.swc_smartpos.network.NetworkWrapper
import it.pagopa.swc_smartpos.network.annotations.Get
import it.pagopa.swc_smartpos.network.annotations.RuntimeUrl
import it.pagopa.swc_smartpos.network.coroutines.utils.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.mockwebserver.MockResponse
import org.junit.Test

class NetworkObserverTest : BaseNetworkTestFactory() {
    private fun response(): String {
        return "{ \"userId\": 1," +
                "  \"id\": 1," +
                "  \"title\": \"myAwesomeTitle\"," +
                "  \"body\": \"myAwesomeBody\"," +
                "\"outcome\": \"ok\"}"
    }

    private fun responseNotOk(): String {
        return "{ \"userId\": 1," +
                "  \"id\": 1," +
                "  \"title\": \"myAwesomeTitle\"," +
                "  \"body\": \"myAwesomeBody\"," +
                "\"outcome\": \"not_ok\"}"
    }

    @Get("change")
    @Test
    fun testWrapper() {
        data class UserJson(val userId: Int, val id: Int, val title: String, val body: String) :
            BaseResponse(), java.io.Serializable
        prepareNetwork(wifi = false, cellular = false, ethernet = true)
        val url = mockServer.url("/").toString()
        mockServer.enqueue(MockResponse().setBody(response()))
        val network =
            BaseNetwork<UserJson>(scope = CoroutineScope(Dispatchers.IO), this.javaClass.name)
        network.call(
            ctx!!,
            UserJson::class.java,
            runtimeUrl = arrayOf(RuntimeUrl("change", url))
        )
        assert(network.data.value is Resource)
        Thread.sleep(5000L)
        network.data.observe(provideLifecycleOwner(), object : NetworkObserver<UserJson> {
            override fun error(message: String?, code: Int?) {
                println("error-> $message, code: $code")
            }

            override fun error401() {
            }

            override fun error200(outcome: String) {
                println("error200-> $outcome")
            }

            override fun loading() {
                println("loading")
            }

            override fun success(t: UserJson?) {
                assert(t is UserJson)
            }
        })
    }

    @Get("change")
    @Test
    fun testNetworkWrapper() {
        data class UserJson(val userId: Int, val id: Int, val title: String, val body: String) :
            BaseResponse(), java.io.Serializable
        prepareNetwork(wifi = false, cellular = false, ethernet = true)
        val url = mockServer.url("/").toString()
        mockServer.enqueue(
            MockResponse().setBody("{\"message\":\"008000001\"").setResponseCode(400)
        )
        val network =
            BaseNetwork<UserJson>(scope = CoroutineScope(Dispatchers.IO), this.javaClass.name)
        network.call(
            ctx!!,
            UserJson::class.java,
            runtimeUrl = arrayOf(RuntimeUrl("change", url))
        )
        assert(network.data.value is Resource)
        Thread.sleep(5000L)
        network.data.observe(provideLifecycleOwner(), NetworkWrapper(
            activity = null,
            successAction = {

            },
            errorAction = {
                assert(it == 400)
            }
        ))

        network.data.observe(
            provideLifecycleOwner(), NetworkWrapper(
                activity = null,
                successAction = {

                },
                errorAction = {
                    assert(it == 400)
                },
                isForQrCode = true
            )
        )
        network.data.observe(
            provideLifecycleOwner(), NetworkWrapper(
                activity = null,
                successAction = {

                },
                showDialog = false,
                errorAction = {
                    assert(it == 400)
                },
                isForQrCode = true
            )
        )
    }

    @Get("change")
    @Test
    fun testWrapper500() {
        data class UserJson(val userId: Int, val id: Int, val title: String, val body: String) :
            BaseResponse(), java.io.Serializable
        prepareNetwork(wifi = false, cellular = false, ethernet = true)
        val url = mockServer.url("/").toString()
        val message = "Internal server error"
        mockServer.enqueue(MockResponse().setResponseCode(500).setBody(message))
        val network =
            BaseNetwork<UserJson>(scope = CoroutineScope(Dispatchers.IO), this.javaClass.name)
        network.call(
            ctx!!,
            UserJson::class.java,
            runtimeUrl = arrayOf(RuntimeUrl("change", url))
        )
        assert(network.data.value is Resource)
        Thread.sleep(5000L)
        network.data.observe(provideLifecycleOwner(), object : NetworkObserver<UserJson> {
            override fun success(t: UserJson?) {}
            override fun error(message: String?, code: Int?) {
                assert(code == 500)
            }

            override fun error401() {
            }

            override fun error200(outcome: String) {
                println("error200-> $outcome")
            }

            override fun loading() {
                println("loading")
            }
        })
    }

    @Get("change")
    @Test
    fun testWrapperNotOk() {
        data class UserJson(val userId: Int, val id: Int, val title: String, val body: String) :
            BaseResponse(), java.io.Serializable
        prepareNetwork(wifi = false, cellular = false, ethernet = true)
        val url = mockServer.url("/").toString()
        mockServer.enqueue(MockResponse().setBody(responseNotOk()))
        val network =
            BaseNetwork<UserJson>(scope = CoroutineScope(Dispatchers.IO), this.javaClass.name)
        network.call(
            ctx!!,
            UserJson::class.java,
            runtimeUrl = arrayOf(RuntimeUrl("change", url))
        )
        assert(network.data.value is Resource)
        Thread.sleep(5000L)
        network.data.observe(provideLifecycleOwner(), object : NetworkObserver<UserJson> {
            override fun loading() {}
            override fun success(t: UserJson?) {}
            override fun error(message: String?, code: Int?) {}
            override fun error401() {
            }

            override fun error200(outcome: String) {
                assert(outcome == "not_ok")
            }
        })
    }
}