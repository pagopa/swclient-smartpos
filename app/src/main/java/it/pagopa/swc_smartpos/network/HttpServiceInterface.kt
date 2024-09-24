package it.pagopa.swc_smartpos.network

import android.content.Context
import androidx.lifecycle.LiveData
import it.pagopa.swc.smartpos.app_shared.model.login.LoginRefreshTokenRequest
import it.pagopa.swc.smartpos.app_shared.model.login.LoginRequest
import it.pagopa.swc.smartpos.app_shared.model.login.LoginRequestPoynt
import it.pagopa.swc.smartpos.app_shared.model.login.LoginResponse
import it.pagopa.swc.smartpos.app_shared.network.BaseHttpServiceInterface
import it.pagopa.swc_smartpos.model.HistoricalTransactionResponse
import it.pagopa.swc_smartpos.model.QrCodeVerifyResponse
import it.pagopa.swc_smartpos.model.Status
import it.pagopa.swc_smartpos.model.activate_payment.ActivatePaymentRequest
import it.pagopa.swc_smartpos.model.activate_payment.ActivatePaymentResponse
import it.pagopa.swc_smartpos.model.close_payment.ClosePaymentPollingResponse
import it.pagopa.swc_smartpos.model.close_payment.ClosePaymentRequest
import it.pagopa.swc_smartpos.model.close_payment.ClosePaymentResponse
import it.pagopa.swc_smartpos.model.fee.RequestFeeRequest
import it.pagopa.swc_smartpos.model.fee.RequestFeeResponse
import it.pagopa.swc_smartpos.model.preclose.PreCloseRequest
import it.pagopa.swc_smartpos.model.preclose.PreCloseResponse
import it.pagopa.swc_smartpos.model.presets.CreatePresetOperationRequest
import it.pagopa.swc_smartpos.model.presets.CreatePresetOperationResponse
import it.pagopa.swc_smartpos.model.presets.PresetOperationsList
import it.pagopa.swc_smartpos.model.presets.subscribe.SubscribeTerminalRequest
import it.pagopa.swc_smartpos.model.presets.subscribe.SubscribeTerminalResponse
import it.pagopa.swc_smartpos.model.presets.terminals.SubscribedTerminalList
import it.pagopa.swc_smartpos.network.annotations.Delete
import it.pagopa.swc_smartpos.network.annotations.Get
import it.pagopa.swc_smartpos.network.annotations.Patch
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

    @Get("/mil-payment-notice/paymentNotices/qr_code")
    fun verifyQrCode(
        context: Context,
        qrCode: String,
        bearer: String,
        currentBusiness: Business?
    ): LiveData<Resource<QrCodeVerifyResponse>> {
        message("verifyQrCode")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<QrCodeVerifyResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                typeToken = QrCodeVerifyResponse::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                ), arrayOf(RuntimeUrl("qr_code", qrCode, isBase64 = true))
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeQrCodeVerify(qrCode, true)
    }

    @Get("/mil-payment-notice/paymentNotices/pa_tax_code/noticeNumber")
    fun verifyPayment(
        context: Context,
        paTaxCode: String,
        noticeNumber: String,
        bearer: String,
        currentBusiness: Business?
    ): LiveData<Resource<QrCodeVerifyResponse>> {
        message("verifyPayment")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<QrCodeVerifyResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                typeToken = QrCodeVerifyResponse::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                ), arrayOf(
                    RuntimeUrl("pa_tax_code", paTaxCode, isPath = true),
                    RuntimeUrl("noticeNumber", noticeNumber, isPath = true)
                )
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeQrCodeVerify(paTaxCode, false)
    }

    @Patch("/mil-payment-notice/paymentNotices/pa_tax_code/noticeNumber")
    fun activateManualPayment(
        context: Context,
        paTaxCode: String,
        noticeNumber: String,
        bearer: String,
        currentBusiness: Business?,
        request: ActivatePaymentRequest
    ): LiveData<Resource<ActivatePaymentResponse>> {
        message("activateManualPayment")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<ActivatePaymentResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                payload = request,
                payloadType = ActivatePaymentRequest::class.java,
                typeToken = ActivatePaymentResponse::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                ), arrayOf(
                    RuntimeUrl("pa_tax_code", paTaxCode, isPath = true),
                    RuntimeUrl("noticeNumber", noticeNumber, isPath = true)
                )
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeActivatePayment(paTaxCode, false)
    }

    @Patch("/mil-payment-notice/paymentNotices/qr_code")
    fun activatePayment(
        context: Context,
        qrCode: String,
        bearer: String,
        currentBusiness: Business?,
        request: ActivatePaymentRequest
    ): LiveData<Resource<ActivatePaymentResponse>> {
        message("activatePayment")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<ActivatePaymentResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                payload = request,
                payloadType = ActivatePaymentRequest::class.java,
                typeToken = ActivatePaymentResponse::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                ), arrayOf(RuntimeUrl("qr_code", qrCode, isBase64 = true))
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeActivatePayment(qrCode, true)
    }

    @Post("/mil-fee-calculator/fees")
    fun requestFee(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        request: RequestFeeRequest
    ): LiveData<Resource<RequestFeeResponse>> {
        message("requestFee")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<RequestFeeResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                payload = request,
                payloadType = RequestFeeRequest::class.java,
                typeToken = RequestFeeResponse::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                )
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeRequestFee()
    }


    @Post("/mil-payment-notice/payments")
    fun preClose(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        request: PreCloseRequest
    ): LiveData<Resource<PreCloseResponse>> {
        message("preClose")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<PreCloseResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                payload = request,
                payloadType = PreCloseRequest::class.java,
                typeToken = PreCloseResponse::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                ), addHeader = request.outcome == Status.PRE_CLOSE.status
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakePreClose()
    }


    @Patch("/mil-payment-notice/payments/transactionId/sendPaymentOutcome")
    fun closePayment(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        request: ClosePaymentRequest,
        transactionId: String
    ): LiveData<Resource<ClosePaymentResponse>> {
        message("closePayment")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<ClosePaymentResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                payload = request,
                payloadType = ClosePaymentRequest::class.java,
                typeToken = ClosePaymentResponse::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                ), addHeader = request.outcome == "CLOSE",
                runtimeUrl = arrayOf(
                    RuntimeUrl("transactionId", transactionId, isPath = true)
                )
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeClosePayment()
    }

    @Get("urlByLocation")
    fun closePaymentPolling(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        url: String
    ): LiveData<Resource<ClosePaymentPollingResponse>> {
        message("closePaymentPolling")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<ClosePaymentPollingResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                typeToken = ClosePaymentPollingResponse::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                ), runtimeUrl = arrayOf(
                    RuntimeUrl("urlByLocation", url)
                )
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeClosePaymentPolling()
    }


    @Get("/mil-payment-notice/payments/transactionId")
    fun closePaymentPollingManually(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        transactionId: String,
    ): LiveData<Resource<ClosePaymentPollingResponse>> {
        message("closePaymentPollingManually")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<ClosePaymentPollingResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                typeToken = ClosePaymentPollingResponse::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                ), runtimeUrl = arrayOf(
                    RuntimeUrl("transactionId", transactionId, isPath = true)
                )
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeClosePaymentPolling()
    }

    @Get("/mil-payment-notice/payments")
    fun getHistoricalTransactions(
        context: Context,
        bearer: String,
        currentBusiness: Business?
    ): LiveData<Resource<HistoricalTransactionResponse>> {
        message("getHistoricalTransactions")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<HistoricalTransactionResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                typeToken = HistoricalTransactionResponse::class.java,
                custom = Api.header(
                    bearer = bearer,
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty()
                )
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeHistoricalTransactions()
    }

    @Post("/mil-preset/terminals")
    fun subscribeTerminal(
        context: Context,
        bearer: String,
        request: SubscribeTerminalRequest,
        currentBusiness: Business?
    ): LiveData<Resource<SubscribeTerminalResponse>> {
        message("subscribeTerminal")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<SubscribeTerminalResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                payload = request,
                payloadType = SubscribeTerminalRequest::class.java,
                typeToken = SubscribeTerminalResponse::class.java,
                custom = Api.header(
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty(),
                    bearer = bearer
                ), addHeader = true
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeSubscribeTerminal()
    }

    @Delete("/mil-preset/terminals/paTaxCode/subscriberId")
    fun unSubScribeTerminal(
        context: Context,
        bearer: String,
        paTaxCode: String,
        subscriberId: String,
        currentBusiness: Business?
    ): LiveData<Resource<EmptyResponse>> {
        message("unSubScribeTerminal")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<EmptyResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                typeToken = EmptyResponse::class.java,
                custom = Api.header(
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty(),
                    bearer = bearer
                ),
                runtimeUrl = arrayOf(
                    RuntimeUrl("paTaxCode", paTaxCode),
                    RuntimeUrl("subscriberId", subscriberId)
                )
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeUnSubScribeTerminal()
    }

    @Get("/mil-preset/terminals/paTaxCode")
    fun getSubscribedTerminals(
        context: Context,
        bearer: String,
        paTaxCode: String,
        currentBusiness: Business?
    ): LiveData<Resource<SubscribedTerminalList>> {
        message("getSubscribedTerminals")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<SubscribedTerminalList>(scope, this.javaClass.name)
            network.call(
                context = context,
                typeToken = SubscribedTerminalList::class.java,
                custom = Api.header(
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty(),
                    bearer = bearer
                ),
                runtimeUrl = arrayOf(RuntimeUrl("paTaxCode", paTaxCode))
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeSubScribeTerminalList()
    }

    @Post("/mil-preset/presets")
    fun createPresetOperation(
        context: Context,
        bearer: String,
        request: CreatePresetOperationRequest,
        currentBusiness: Business?
    ): LiveData<Resource<CreatePresetOperationResponse>> {
        message("createPresetOperation")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<CreatePresetOperationResponse>(scope, this.javaClass.name)
            network.call(
                context = context,
                payload = request,
                payloadType = CreatePresetOperationRequest::class.java,
                typeToken = CreatePresetOperationResponse::class.java,
                custom = Api.header(
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty(),
                    bearer = bearer
                ), addHeader = true
            )
            network.data
        } else
            HttpServiceInterfaceMocked().fakeCreatePresetOperation()
    }

    @Get("/mil-preset/presets/paTaxCode/subscriberId")
    fun getPresetOperations(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        paTaxCode: String,
        subscriberId: String
    ): LiveData<Resource<PresetOperationsList>> {
        message("getPresetOperations")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<PresetOperationsList>(scope, this.javaClass.name)
            network.call(
                context = context,
                typeToken = PresetOperationsList::class.java,
                custom = Api.header(
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty(),
                    bearer = bearer
                ),
                runtimeUrl = arrayOf(
                    RuntimeUrl("paTaxCode", paTaxCode),
                    RuntimeUrl("subscriberId", subscriberId)
                )
            )
            network.data
        } else
            HttpServiceInterfaceMocked().presetsListMocked()
    }

    @Get("/mil-preset/presets/paTaxCode/subscriberId/last_to_execute")
    fun getLastPresetOperation(
        context: Context,
        bearer: String,
        currentBusiness: Business?,
        paTaxCode: String,
        subscriberId: String
    ): LiveData<Resource<PresetOperationsList.Preset>> {
        message("getLastPresetOperation")
        return if (!NetworkLogger.isMockEnv) {
            val network = BaseNetwork<PresetOperationsList.Preset>(scope, this.javaClass.name)
            network.call(
                context = context,
                typeToken = PresetOperationsList.Preset::class.java,
                custom = Api.header(
                    acquirerId = currentBusiness?.acquirerIdList?.getOrNull(0).orEmpty(),
                    terminalId = currentBusiness?.terminalId.orEmpty(),
                    merchantId = currentBusiness?.merchantId.orEmpty(),
                    bearer = bearer
                ),
                runtimeUrl = arrayOf(
                    RuntimeUrl("paTaxCode", paTaxCode),
                    RuntimeUrl("subscriberId", subscriberId)
                )
            )
            network.data
        } else
            HttpServiceInterfaceMocked().lastPresetMocked()
    }
}