package it.pagopa.swc_smartpos.network.errors

import it.pagopa.swc_smartpos.network.BaseNetwork
import it.pagopa.swc_smartpos.network.BaseNetworkTestFactory
import it.pagopa.swc_smartpos.network.UserJson
import it.pagopa.swc_smartpos.network.annotations.Get
import it.pagopa.swc_smartpos.network.coroutines.utils.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Test

class NoHttpUrl : BaseNetworkTestFactory() {
    @Test
    @Get("test")
    fun noHttp() {
        prepareNetwork()
        val network = BaseNetwork<Array<UserJson>>(scope = CoroutineScope(Dispatchers.IO), this.javaClass.name)
        network.call(
            ctx!!,
            Array<UserJson>::class.java
        )
        Thread.sleep(1000L)
        network.data.observe(provideLifecycleOwner()) {
            assert(it.status == Status.ERROR)
            assert(it.message!!.contains("MalformedUrlException", true))
        }
    }
}