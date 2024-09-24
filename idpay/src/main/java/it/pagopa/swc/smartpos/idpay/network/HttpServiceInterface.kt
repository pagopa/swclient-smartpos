package it.pagopa.swc.smartpos.idpay.network

import android.content.Context
import androidx.lifecycle.LiveData
import it.pagopa.swc.smartpos.app_shared.model.login.LoginRefreshTokenRequest
import it.pagopa.swc.smartpos.app_shared.model.login.LoginRequest
import it.pagopa.swc.smartpos.app_shared.model.login.LoginRequestPoynt
import it.pagopa.swc.smartpos.app_shared.model.login.LoginResponse
import it.pagopa.swc.smartpos.app_shared.network.BaseHttpServiceInterface
import it.pagopa.swc.smartpos.idpay.model.Initiatives
import it.pagopa.swc.smartpos.idpay.model.request.AuthorizeRequest
import it.pagopa.swc.smartpos.idpay.model.request.CreateTransactionRequest
import it.pagopa.swc.smartpos.idpay.model.request.VerifyCieRequest
import it.pagopa.swc.smartpos.idpay.model.response.CreateTransactionResponse
import it.pagopa.swc.smartpos.idpay.model.response.HistoricalTransactionsResponse
import it.pagopa.swc.smartpos.idpay.model.response.Transaction
import it.pagopa.swc.smartpos.idpay.model.response.TransactionDetailResponse
import it.pagopa.swc.smartpos.idpay.model.response.VerifyCieResponse
import it.pagopa.swc_smartpos.network.BaseNetwork
import it.pagopa.swc_smartpos.network.EmptyResponse
import it.pagopa.swc_smartpos.network.annotations.Delete
import it.pagopa.swc_smartpos.network.annotations.Get
import it.pagopa.swc_smartpos.network.annotations.Post
import it.pagopa.swc_smartpos.network.annotations.RuntimeUrl
import it.pagopa.swc_smartpos.network.coroutines.utils.NetworkLogger
import it.pagopa.swc_smartpos.network.coroutines.utils.Resource
import it.pagopa.swc_smartpos.sharedutils.model.Business
import kotlinx.coroutines.CoroutineScope

class HttpServiceInterface(private val scope: CoroutineScope) : BaseHttpServiceInterface() {
    @Post("/mil-auth/token")
    override fun login(
        context: Context,
        request: LoginRequest,
        currentBusiness: Business?
    ): LiveData<Resource<LoginResponse>> {
        message("Login")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<LoginResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                payload = request,
                payloadType = LoginRequest::class.java,
                typeToken = LoginResponse::class.java,
                custom = Api.loginHeader(
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                )
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeLogin(request)
    }

    @Post("/mil-auth/token")
    override fun loginPoynt(
        context: Context,
        request: LoginRequestPoynt,
        currentBusiness: Business?
    ): LiveData<Resource<LoginResponse>> {
        message("LoginPoynt")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<LoginResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                payload = request,
                payloadType = LoginRequestPoynt::class.java,
                typeToken = LoginResponse::class.java,
                custom = Api.loginHeader(
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                )
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeLoginPoynt()
    }

    @Post("/mil-auth/token")
    override fun refreshToken(
        context: Context,
        bearer: String,
        request: LoginRefreshTokenRequest,
        currentBusiness: Business?
    ): LiveData<Resource<LoginResponse>> {
        message("refreshToken")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<LoginResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                payload = request,
                payloadType = LoginRefreshTokenRequest::class.java,
                typeToken = LoginResponse::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty(),
                    forRefreshToken = true
                )
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeRefreshToken(request)
    }

    @Get("/mil-idpay/initiatives")
    fun retrieveInitiative(
        context: Context,
        bearer: String,
        currentBusiness: Business?
    ): LiveData<Resource<Initiatives>> {
        message("retrieving initiatives")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<Initiatives>(scope, this.javaClass.name)
            network.call(
                context = context,
                typeToken = Initiatives::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                )
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeInitiativeList()
    }

    @Post("/mil-idpay/transactions")
    fun createTransaction(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        request: CreateTransactionRequest
    ): LiveData<Resource<CreateTransactionResponse>> {
        message("creating transaction")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<CreateTransactionResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                payload = request,
                payloadType = CreateTransactionRequest::class.java,
                typeToken = CreateTransactionResponse::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty(),
                ),
                addHeader = true
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeCreateTransaction(request)
    }

    @Get("/mil-idpay/transactions/milTransactionId")
    fun idPayTransactionDetail(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        transactionId: String
    ): LiveData<Resource<TransactionDetailResponse>> {
        message("idPayTransactionDetail")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<TransactionDetailResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                typeToken = TransactionDetailResponse::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                ),
                runtimeUrl = arrayOf(RuntimeUrl("milTransactionId", transactionId))
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeTransactionDetail()
    }

    @Post("/mil-idpay/transactions/milTransactionId/verifyCie")
    fun verifyCie(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        request: VerifyCieRequest,
        transactionId: String
    ): LiveData<Resource<VerifyCieResponse>> {
        message("verifying cie..")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<VerifyCieResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                payload = request,
                payloadType = VerifyCieRequest::class.java,
                typeToken = VerifyCieResponse::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                ),
                runtimeUrl = arrayOf(RuntimeUrl("milTransactionId", transactionId)),
                disableHtmlEscaping = true
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeCieVerify()
    }

    @Post("/mil-idpay/transactions/milTransactionId/authorize")
    fun authorize(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        request: AuthorizeRequest,
        transactionId: String
    ): LiveData<Resource<EmptyResponse>> {
        message("authorize request..; payload: $request")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<EmptyResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                payload = request,
                payloadType = AuthorizeRequest::class.java,
                typeToken = EmptyResponse::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                ),
                runtimeUrl = arrayOf(RuntimeUrl("milTransactionId", transactionId)),
                disableHtmlEscaping = true
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeAuthorize()
    }

    @Delete("/mil-idpay/transactions/milTransactionId")
    fun deleteTransaction(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        transactionId: String
    ): LiveData<Resource<Transaction>> {
        message("deleting transaction: $transactionId")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<Transaction>(scope, this.javaClass.name)
            network.call(
                context = context,
                typeToken = Transaction::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                ),
                runtimeUrl = arrayOf(RuntimeUrl("milTransactionId", transactionId))
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeDelete()
    }

    @Get("/mil-idpay/transactions")
    fun getHistoricalTransactions(
        context: Context,
        bearer: String,
        currentBusiness: Business?
    ): LiveData<Resource<HistoricalTransactionsResponse>> {
        message("getHistoricalTransactions")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<HistoricalTransactionsResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                typeToken = HistoricalTransactionsResponse::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                )
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeTransactionHistory()
    }
}