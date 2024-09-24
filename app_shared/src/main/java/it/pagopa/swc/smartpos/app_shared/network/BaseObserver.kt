package it.pagopa.swc.smartpos.app_shared.network

import androidx.lifecycle.Observer
import it.pagopa.swc_smartpos.network.coroutines.utils.Resource
import it.pagopa.swc_smartpos.network.coroutines.utils.Status

interface BaseObserver<Data> : Observer<Resource<Data>> {
    fun loading()
    fun success(t: Data?)
    fun error(message: String?, code: Int?)
    fun error401()
    override fun onChanged(value: Resource<Data>) {
        when (value.status) {
            Status.LOADING -> loading()
            Status.ERROR -> {
                if (value.code != 401)
                    error(value.message, value.code)
                else
                    error401()
            }
            Status.SUCCESS -> success(value.data)
        }
    }
}