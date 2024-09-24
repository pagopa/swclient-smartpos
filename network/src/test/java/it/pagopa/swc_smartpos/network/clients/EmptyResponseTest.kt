package it.pagopa.swc_smartpos.network.clients

import it.pagopa.swc_smartpos.network.*
import it.pagopa.swc_smartpos.network.annotations.Patch
import it.pagopa.swc_smartpos.network.annotations.RuntimeUrl
import it.pagopa.swc_smartpos.network.coroutines.utils.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert
import org.junit.Test

class EmptyResponseTest : BaseNetworkTestFactory() {
    @Test
    @Patch("change")
    fun httpNoContentTest() {
        setSdkInt(23)
        prepareNetwork()
        val url = mockServer.url("/").toString()
        mockServer.enqueue(MockResponse().setResponseCode(204))
        val network = BaseNetwork<EmptyResponse>(scope = CoroutineScope(Dispatchers.IO), this.javaClass.name)
        network.call(
            ctx!!,
            OtherUserJson("ciao", "test"),
            OtherUserJson::class.java,
            EmptyResponse::class.java,
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
                    Assert.assertEquals(it.data is EmptyResponse, true)
                    Assert.assertEquals((it.data as EmptyResponse).body, "")
                }
            }
        }
    }
}