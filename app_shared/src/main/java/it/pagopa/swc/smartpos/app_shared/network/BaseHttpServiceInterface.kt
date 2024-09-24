package it.pagopa.swc.smartpos.app_shared.network

import android.content.Context
import androidx.lifecycle.LiveData
import it.pagopa.swc.smartpos.app_shared.model.login.LoginRefreshTokenRequest
import it.pagopa.swc.smartpos.app_shared.model.login.LoginRequest
import it.pagopa.swc.smartpos.app_shared.model.login.LoginRequestPoynt
import it.pagopa.swc.smartpos.app_shared.model.login.LoginResponse
import it.pagopa.swc_smartpos.network.coroutines.utils.NetworkLogger
import it.pagopa.swc_smartpos.network.coroutines.utils.Resource
import it.pagopa.swc_smartpos.sharedutils.model.Business

abstract class BaseHttpServiceInterface {
    fun message(service: String) = NetworkLogger.i("HttpServiceInterface", "calling $service...")
    abstract fun login(
        context: Context,
        request: LoginRequest,
        currentBusiness: Business?
    ): LiveData<Resource<LoginResponse>>

    abstract fun loginPoynt(
        context: Context,
        request: LoginRequestPoynt,
        currentBusiness: Business?
    ): LiveData<Resource<LoginResponse>>

    abstract fun refreshToken(
        context: Context,
        bearer: String,
        request: LoginRefreshTokenRequest,
        currentBusiness: Business?
    ): LiveData<Resource<LoginResponse>>
}