package it.pagopa.swc_smartpos.network.errors

import it.pagopa.swc_smartpos.network.BaseNetwork
import it.pagopa.swc_smartpos.network.BaseNetworkTestFactory
import it.pagopa.swc_smartpos.network.UserJson
import it.pagopa.swc_smartpos.network.annotations.Get
import it.pagopa.swc_smartpos.network.annotations.RuntimeUrl
import it.pagopa.swc_smartpos.network.connection.ConnectionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert
import org.junit.Test

class NoConnectivity : BaseNetworkTestFactory() {
    @Test
    @Get("change")
    fun noTransport() {
        setSdkInt(23)
        prepareNetwork(wifi = false, cellular = false, ethernet = false)
        Assert.assertEquals(ConnectionHelper(ctx!!).isConnectionAvailable(), false)
        val network = BaseNetwork<Array<UserJson>>(scope = CoroutineScope(Dispatchers.IO), this.javaClass.name)
        network.call(
            ctx!!,
            Array<UserJson>::class.java,
            runtimeUrl = arrayOf(RuntimeUrl("change", ""))
        )
        Thread.sleep(1000L)
        network.data.observe(provideLifecycleOwner()) {
            Assert.assertEquals(it.message == "no network available", true)
        }
    }
}