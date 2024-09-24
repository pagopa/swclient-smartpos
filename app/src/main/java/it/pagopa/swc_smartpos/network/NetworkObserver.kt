package it.pagopa.swc_smartpos.network

import it.pagopa.swc.smartpos.app_shared.network.BaseObserver
import it.pagopa.swc_smartpos.model.BaseResponse
import it.pagopa.swc_smartpos.network.coroutines.utils.Resource
import it.pagopa.swc_smartpos.network.coroutines.utils.Status
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger

/**Observer implemented by [NetworkWrapper] to observe live data from network*/
interface NetworkObserver<Data : BaseResponse> : BaseObserver<Data> {
    fun error200(outcome: String)
    override fun onChanged(value: Resource<Data>) {
        when (value.status) {
            Status.LOADING -> loading()
            Status.ERROR -> {
                if (value.code != 401)
                    error(value.message, value.code)
                else
                    error401()
            }
            Status.SUCCESS -> {
                if (value.data?.outcome?.equals("ok", true) == true) {
                    success(value.data)
                } else {
                    error200(value.data?.outcome.orEmpty())
                    WrapperLogger.e("challenge err.", value.data?.outcome.orEmpty())
                }
            }
        }
    }
}