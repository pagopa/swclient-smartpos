package it.pagopa.swc.smartpos.app_shared.login

import it.pagopa.swc.smartpos.app_shared.BaseMainActivity
import it.pagopa.swc.smartpos.app_shared.model.login.LoginRefreshTokenRequest
import it.pagopa.swc.smartpos.app_shared.model.login.LoginRequestPoynt
import it.pagopa.swc.smartpos.app_shared.network.BaseHttpServiceInterface
import it.pagopa.swc_smartpos.network.coroutines.utils.Status
import it.pagopa.swc_smartpos.sharedutils.encryption.decryptWith
import it.pagopa.swc_smartpos.sharedutils.extensions.Token
import it.pagopa.swc_smartpos.sharedutils.extensions.toExpirationTime
import it.pagopa.swc_smartpos.sharedutils.model.Business
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.Date
import java.util.Properties

class LoginUtility(private val mainActivity: BaseMainActivity<*>?, private val httpServiceInterface: BaseHttpServiceInterface) {
    fun getLoginValidationMap(): HashMap<LoginValidation, String>? {
        val properties = Properties()
        val inputStream: InputStream? = mainActivity?.javaClass?.classLoader?.getResourceAsStream("assets/secrets.properties")
        runCatching {
            properties.load(inputStream)
        }.onSuccess {
            return hashMapOf(
                LoginValidation.ClientId to properties.getProperty("client_id"),
                LoginValidation.GrantType to properties.getProperty("grant_type"),
                LoginValidation.GrantTypePoynt to properties.getProperty("grant_type_poynt"),
                LoginValidation.Scope to properties.getProperty("scope")
            )
        }
        return null
    }

    enum class LoginValidation {
        ClientId,
        GrantType,
        GrantTypePoynt,
        Scope
    }

    fun refreshTokenCtrl(action: (Boolean) -> Unit) {
        mainActivity?.let {
            it.viewModel.refreshToken.value.decryptWith(it.keyStore) { refresh ->
                if (Date().time > refresh.toExpirationTime())
                    action.invoke(false)
                else
                    action.invoke(true)
            }
        } ?: run {
            action.invoke(false)
        }
    }

    fun poyntTokenCtrl(): Boolean {
        mainActivity?.sdkUtils?.getToken()?.value?.peekContent()?.accessToken?.toExpirationTime()?.let { poyntTknExpTIme ->
            return Date().time > poyntTknExpTIme
        } ?: run { return false }
    }

    fun tokenCtrl(isMock: Boolean, toDo: (ActionToDo) -> Unit) {
        mainActivity?.let {
            val now = Date().time
            val token = it.viewModel.accessToken.value
            if (token?.size == 1 && token[0] == "") {
                toDo.invoke(ActionToDo.FirstToken)
            } else {
                it.refreshingSessionDialog.isCancelable = false
                it.refreshingSessionDialog.loading(true)
                it.refreshingSessionDialog.showDialog(it.supportFragmentManager)
                token.decryptWith(it.keyStore) { tkn ->
                    if (isMock) {
                        var job: Job? = null
                        job = CoroutineScope(Dispatchers.Default).launch {
                            delay(2000L)
                            toDo.invoke(ActionToDo.ValidToken)
                            job?.cancel()
                        }
                        job.start()
                    } else {
                        if (now > tkn.toExpirationTime()) {
                            refreshTokenCtrl { isValid ->
                                if (isValid)
                                    toDo.invoke(ActionToDo.RefreshToken)
                                else
                                    toDo.invoke(ActionToDo.TokenReallyExpired)
                            }
                        } else
                            toDo.invoke(ActionToDo.ValidToken)
                    }
                }
            }
        }
    }

    fun callTokenPoynt(poyntTkn: Token, business: Business?, action: (itsOk: Boolean) -> Unit) {
        mainActivity?.let { activity ->
            val map = getLoginValidationMap()
            httpServiceInterface
                .loginPoynt(
                    activity, LoginRequestPoynt(
                        extToken = poyntTkn,
                        addData = business?.id.orEmpty(),
                        clientId = map?.get(LoginValidation.ClientId).orEmpty(),
                        grantType = map?.get(LoginValidation.GrantTypePoynt).orEmpty(),
                        scope = map?.get(LoginValidation.Scope).orEmpty()
                    ), business
                ).observe(activity) {
                    when (it.status) {
                        Status.LOADING -> {//do nothing
                        }

                        Status.SUCCESS -> {
                            activity.encrypt(it.data?.accessToken.orEmpty()) { listEncrypted ->
                                activity.viewModel.setAccessToken(listEncrypted)
                                activity.encrypt(it.data?.refreshToken.orEmpty()) { listRefreshEncrypted ->
                                    activity.viewModel.setRefreshToken(listRefreshEncrypted)
                                    action.invoke(true)
                                }
                            }
                        }

                        Status.ERROR -> action.invoke(false)
                    }
                }
        }
    }

    fun refreshToken(action: (Boolean) -> Unit) {
        mainActivity?.let {
            val loginMap = getLoginValidationMap()
            it.decrypt(it.viewModel.accessToken.value) { accessToken ->
                it.decrypt(it.viewModel.refreshToken.value) { refreshToken ->
                    httpServiceInterface.refreshToken(
                        it,
                        accessToken, LoginRefreshTokenRequest(
                            loginMap?.get(LoginValidation.ClientId).orEmpty(),
                            "refresh_token", refreshToken
                        ),
                        it.sdkUtils?.getCurrentBusiness()?.value
                    ).observe(it) { res ->
                        when (res.status) {
                            Status.ERROR -> action.invoke(false)
                            Status.SUCCESS -> {
                                it.encrypt(res.data?.accessToken.orEmpty()) { listEncrypted ->
                                    it.viewModel.setAccessToken(listEncrypted)
                                    it.encrypt(res.data?.refreshToken.orEmpty()) { listRefreshEncrypted ->
                                        it.viewModel.setRefreshToken(listRefreshEncrypted)
                                        action.invoke(true)
                                    }
                                }
                            }

                            Status.LOADING -> {//do nothing
                            }
                        }
                    }
                }
            }
        }
    }

    enum class ActionToDo {
        ValidToken,
        FirstToken,
        RefreshToken,
        TokenReallyExpired
    }

    companion object {
        var shouldVerifySession = true
    }
}