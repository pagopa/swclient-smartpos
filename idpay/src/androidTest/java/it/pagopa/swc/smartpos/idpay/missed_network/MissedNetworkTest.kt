package it.pagopa.swc.smartpos.idpay.missed_network

import it.pagopa.swc.smartpos.app_shared.model.login.LoginRefreshTokenRequest
import it.pagopa.swc.smartpos.app_shared.model.login.LoginResponse
import it.pagopa.swc.smartpos.app_shared.network.BaseObserver
import it.pagopa.swc.smartpos.idpay.flow.BaseFlowTest
import it.pagopa.swc.smartpos.idpay.network.HttpServiceInterfaceMocked
import it.pagopa.swc_smartpos.network.coroutines.utils.Status
import org.junit.Test

class MissedNetworkTest : BaseFlowTest() {
    override fun launchActivity() {
        super.launchActivity()
        val loginPoyntLiveData = HttpServiceInterfaceMocked().fakeLoginPoynt()
        currentActivity!!.runOnUiThread {
            loginPoyntLiveData.observe(currentActivity!!, object : BaseObserver<LoginResponse> {
                override fun error(message: String?, code: Int?) {}
                override fun error401() {}
                override fun loading() {
                    assert(loginPoyntLiveData.value?.status == Status.LOADING)
                }

                override fun success(t: LoginResponse?) {
                    assert(loginPoyntLiveData.value?.data?.accessToken == "fakeBearer")
                }
            })
        }
        networkSleep()
    }

    @Test
    fun refreshTokenReq() {
        super.launchActivity()
        val refreshTokenReq = LoginRefreshTokenRequest("fake", "fake", "fakeRefresh")
        val refreshTokenCall = HttpServiceInterfaceMocked().fakeRefreshToken(refreshTokenReq)
        currentActivity!!.runOnUiThread {
            refreshTokenCall.observe(currentActivity!!, object : BaseObserver<LoginResponse> {
                override fun error(message: String?, code: Int?) {}
                override fun error401() {}
                override fun loading() {
                    assert(refreshTokenCall.value?.status == Status.LOADING)
                }

                override fun success(t: LoginResponse?) {
                    assert(refreshTokenCall.value?.data?.accessToken == "fakeBearer")
                }
            })
        }
        val refreshTokenReq2 = LoginRefreshTokenRequest("fake", "fake", "bla")
        val refreshTokenCall2 = HttpServiceInterfaceMocked().fakeRefreshToken(refreshTokenReq2)
        currentActivity!!.runOnUiThread {
            refreshTokenCall2.observe(currentActivity!!, object : BaseObserver<LoginResponse> {
                override fun success(t: LoginResponse?) {}
                override fun error(message: String?, code: Int?) {
                    assert(message == "No valid refresh token")
                }

                override fun error401() {}
                override fun loading() {
                    assert(refreshTokenCall.value?.status == Status.LOADING)
                }
            })
        }
        networkSleep()
    }
}