package it.pagopa.swc_smartpos.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataScope
import androidx.lifecycle.liveData
import com.google.gson.Gson
import it.pagopa.swc.smartpos.app_shared.model.login.LoginRefreshTokenRequest
import it.pagopa.swc.smartpos.app_shared.model.login.LoginRequest
import it.pagopa.swc.smartpos.app_shared.model.login.LoginResponse
import it.pagopa.swc_smartpos.model.HistoricalTransactionResponse
import it.pagopa.swc_smartpos.model.Notice
import it.pagopa.swc_smartpos.model.QrCodeVerifyResponse
import it.pagopa.swc_smartpos.model.Status
import it.pagopa.swc_smartpos.model.Transaction
import it.pagopa.swc_smartpos.model.Transfer
import it.pagopa.swc_smartpos.model.activate_payment.ActivatePaymentResponse
import it.pagopa.swc_smartpos.model.close_payment.ClosePaymentPollingResponse
import it.pagopa.swc_smartpos.model.close_payment.ClosePaymentResponse
import it.pagopa.swc_smartpos.model.fee.RequestFeeResponse
import it.pagopa.swc_smartpos.model.preclose.PreCloseResponse
import it.pagopa.swc_smartpos.model.presets.CreatePresetOperationResponse
import it.pagopa.swc_smartpos.model.presets.PresetOperationsList
import it.pagopa.swc_smartpos.model.presets.subscribe.SubscribeTerminalResponse
import it.pagopa.swc_smartpos.model.presets.terminals.SubscribedTerminalList
import it.pagopa.swc_smartpos.network.coroutines.utils.Resource
import kotlinx.coroutines.delay
import java.util.Date

class HttpServiceInterfaceMocked {
    private suspend fun <Obj> LiveDataScope<Resource<Obj>>.fakeSuccessCall(obj: Obj) {
        emit(Resource.loading())
        delay(2000L)
        emit(Resource.success(200, obj))
    }

    private suspend infix fun <Obj> LiveDataScope<Resource<Obj>>.fakeErrorCallWithMessage(message: String) {
        emit(Resource.loading())
        delay(2000L)
        emit(Resource.error(500, message))
    }

    private fun String.withOutcomeOk(): String {
        return this.substring(0, this.length - 1) + ",\"outcome\":\"OK\"}"
    }

    private infix fun String.withOutcome(what: String): String {
        return this.substring(0, this.length - 1) + ",\"outcome\":\"$what\"}"
    }

    fun fakeLogin(request: LoginRequest) = liveData<Resource<LoginResponse>> {
        if (request.password == "access") {
            this.fakeSuccessCall(
                LoginResponse(
                    "fakeBearer",
                    200000, "fakeRefresh", "Token"
                )
            )
        } else
            this fakeErrorCallWithMessage "No valid credentials"
    }

    fun fakeLoginPoynt() = liveData {
        this.fakeSuccessCall(
            LoginResponse(
                "fakeBearer",
                200000, "fakeRefresh", "Token"
            )
        )
    }

    fun fakeRefreshToken(request: LoginRefreshTokenRequest) = liveData<Resource<LoginResponse>> {
        if (request.refreshToken == "fakeRefresh") {
            this.fakeSuccessCall(
                LoginResponse(
                    "fakeBearer",
                    200000, "fakeRefresh", "Token"
                )
            )
        } else
            this fakeErrorCallWithMessage "No valid refresh token"
    }

    fun fakeQrCodeVerify(qrCode: String, isQrCode: Boolean): LiveData<Resource<QrCodeVerifyResponse>> {
        val resp = QrCodeVerifyResponse(
            12000, "mockedCompany", "mockedDescription", "302051234567890123", "000000000201", "mockedNote", "mockedOffice"
        )
        return respondWithQrCodeErrors(resp, qrCode, isQrCode, false)
    }

    private inline fun <reified T> respondWithQrCodeErrors(
        resp: T,
        qrCode: String,
        isQrCode: Boolean,
        isActivate: Boolean
    ) = liveData {
        val respJson = Gson().toJson(resp)
        val noticeGlitch = (if (isActivate) "00000000101" else "00000000001") to (respJson withOutcome Api.ErrorCode.NOTICE_GLITCH)
        val wrongNoticeData = (if (isActivate) "00000000102" else "00000000002") to (respJson withOutcome Api.ErrorCode.WRONG_NOTICE_DATA)
        val creditorProblems = (if (isActivate) "00000000103" else "00000000003") to (respJson withOutcome Api.ErrorCode.CREDITOR_PROBLEMS)
        val paymentAlreadyInProgress = (if (isActivate) "00000000104" else "00000000004") to (respJson withOutcome Api.ErrorCode.PAYMENT_ALREADY_IN_PROGRESS)
        val expiredNotice = (if (isActivate) "00000000105" else "00000000005") to (respJson withOutcome Api.ErrorCode.EXPIRED_NOTICE)
        val unknownNotice = (if (isActivate) "00000000106" else "00000000006") to (respJson withOutcome Api.ErrorCode.UNKNOWN_NOTICE)
        val revokedNotice = (if (isActivate) "00000000107" else "00000000007") to (respJson withOutcome Api.ErrorCode.REVOKED_NOTICE)
        val noticeAlreadyPaid = (if (isActivate) "00000000108" else "00000000008") to (respJson withOutcome Api.ErrorCode.NOTICE_ALREADY_PAID)
        val unexpectedError = (if (isActivate) "00000000109" else "00000000009") to (respJson withOutcome "UNEXPECTED_ERROR")
        val allResp = respJson.withOutcomeOk()
        val qrCodeSplit = qrCode.split("|")
        val toCheck = if (isQrCode)
            qrCodeSplit[3]
        else
            qrCode
        when (toCheck) {
            noticeGlitch.first -> this.fakeSuccessCall(Gson().fromJson(noticeGlitch.second, T::class.java))
            wrongNoticeData.first -> this.fakeSuccessCall(Gson().fromJson(wrongNoticeData.second, T::class.java))
            creditorProblems.first -> this.fakeSuccessCall(Gson().fromJson(creditorProblems.second, T::class.java))
            paymentAlreadyInProgress.first -> this.fakeSuccessCall(Gson().fromJson(paymentAlreadyInProgress.second, T::class.java))
            expiredNotice.first -> this.fakeSuccessCall(Gson().fromJson(expiredNotice.second, T::class.java))
            unknownNotice.first -> this.fakeSuccessCall(Gson().fromJson(unknownNotice.second, T::class.java))
            revokedNotice.first -> this.fakeSuccessCall(Gson().fromJson(revokedNotice.second, T::class.java))
            noticeAlreadyPaid.first -> this.fakeSuccessCall(Gson().fromJson(noticeAlreadyPaid.second, T::class.java))
            unexpectedError.first -> this.fakeSuccessCall(Gson().fromJson(unexpectedError.second, T::class.java))
            else -> this.fakeSuccessCall(Gson().fromJson(allResp, T::class.java))
        }
    }

    fun fakeActivatePayment(qrCode: String, isQrCode: Boolean): LiveData<Resource<ActivatePaymentResponse>> {
        val resp = ActivatePaymentResponse(
            12000, "302051234567890123", "fakePaymentToken",
            listOf(Transfer("302051234567890123", "fakeCategory"))
        )
        return respondWithQrCodeErrors(resp, qrCode, isQrCode, true)
    }

    fun fakeRequestFee() = liveData {
        val resp = RequestFeeResponse(100)
        val respJson = Gson().toJson(resp)
        val allResp = respJson.withOutcomeOk()
        this.fakeSuccessCall(Gson().fromJson(allResp, RequestFeeResponse::class.java))
    }

    fun fakePreClose() = liveData {
        val resp = PreCloseResponse(listOf("fakePreCloseLocation"))
        val respJson = Gson().toJson(resp)
        val allResp = respJson.withOutcomeOk()
        this.fakeSuccessCall(Gson().fromJson(allResp, PreCloseResponse::class.java))
    }

    fun fakeClosePayment() = liveData {
        val resp = ClosePaymentResponse(listOf("fakeUrl"), listOf(15), listOf(3))
        val respJson = Gson().toJson(resp)
        val allResp = respJson.withOutcomeOk()
        this.fakeSuccessCall(Gson().fromJson(allResp, ClosePaymentResponse::class.java))
    }

    fun fakeClosePaymentPolling() = liveData {
        val resp = ClosePaymentPollingResponse(
            "fakeAcquirerId", "fakeChannel", 100, Date().time.toString(),
            "fakeMerchantId", listOf(
                Notice(
                    12000, "fakeCompany", "fakeDescription", "00000000201",
                    "fakeOffice", "302051234567890123", "fakePaymentToken"
                )
            ),
            Status.CLOSED.status,
            "30390022",
            12100, "fakeTransactionId"
        )
        this.fakeSuccessCall(Gson().fromJson(Gson().toJson(resp), ClosePaymentPollingResponse::class.java))
    }

    fun fakeHistoricalTransactions() = liveData {
        val resp = HistoricalTransactionResponse(transactionsHistoryMocked())
        val respJson = Gson().toJson(resp)
        val allResp = respJson.withOutcomeOk()
        this.fakeSuccessCall(Gson().fromJson(allResp, HistoricalTransactionResponse::class.java))
    }

    //UTILS
    private val oneNotice = Notice(
        company = "CompanyName",
        paTaxCode = "00000000201",
        description = "fakeDescription",
        noticeNumber = "302051234567890123",
        amount = 12345
    )

    private fun transactionsHistoryMocked(): ArrayList<Transaction> {
        val listMocked = ArrayList<Transaction>()
        val date = "2023-04-11T16:20:34"
        val executedTransaction = Transaction(
            transactionId = "56123168650_qW1tpa1Nw4",
            status = Status.CLOSED.status,
            insertTimestamp = date,
            totalAmount = 10000,
            fee = 100,
            notices = listOf(oneNotice)
        )
        val transactionAborted = Transaction(
            transactionId = "56123168650_qW1tpa1Nw4",
            status = Status.ABORT.status,
            insertTimestamp = date,
            totalAmount = 10000,
            fee = 100,
            notices = listOf(oneNotice)
        )
        val transactionError = Transaction(
            transactionId = "56123168650_qW1tpa1Nw4",
            status = Status.ERROR_ON_CLOSE.status,
            insertTimestamp = date,
            totalAmount = 10000,
            fee = 100,
            notices = listOf(oneNotice)
        )
        val refundTransaction = Transaction(
            transactionId = "56123168650_qW1tpa1Nw4",
            status = Status.RIMBORSATA.status,
            insertTimestamp = date,
            totalAmount = 10000,
            fee = 100,
            notices = listOf(oneNotice)
        )
        val transactionPending = Transaction(
            transactionId = "56123168650_qW1tpa1Nw4",
            status = Status.PENDING.status,
            insertTimestamp = date,
            totalAmount = 10000,
            fee = 100,
            notices = listOf(oneNotice)
        )

        val transactionPreClose = Transaction(
            transactionId = "56123168650_qW1tpa1Nw4",
            status = Status.PRE_CLOSE.status,
            insertTimestamp = date,
            totalAmount = 10000,
            fee = 100,
            notices = listOf(oneNotice)
        )
        for (i in 0..3) {
            listMocked.add(executedTransaction)
            listMocked.add(transactionAborted)
            listMocked.add(transactionError)
            listMocked.add(refundTransaction)
            listMocked.add(transactionPending)
            listMocked.add(transactionPreClose)
        }
        return listMocked
    }

    fun fakeSubscribeTerminal() = liveData {
        val resp = SubscribeTerminalResponse(listOf("mocked/subscriberId"))
        this.fakeSuccessCall(resp)
    }

    fun fakeUnSubScribeTerminal() = liveData {
        this.fakeSuccessCall(EmptyResponse(""))
    }

    fun fakeSubScribeTerminalList() = liveData {
        val resp = SubscribedTerminalList(
            listOf(
                SubscribedTerminalList.Subscriber(
                    "fakeAcquirerId", "fakeChannel", "fakeLabel", "1683816455000", "fakeMerchantId", "fakePaTaxCode",
                    "fakeSubscriberId", "1681217254000", "fakeTerminalId"
                ), SubscribedTerminalList.Subscriber(
                    "fakeAcquirerId2", "fakeChannel2", "fakeLabel2", "16838164550002", "fakeMerchantId2", "fakePaTaxCode2",
                    "fakeSubscriberId2", "16812172540002", "fakeTerminalId2"
                )
            )
        )
        this.fakeSuccessCall(resp)
    }

    fun fakeCreatePresetOperation() = liveData {
        val resp = CreatePresetOperationResponse(listOf("mocked"))
        this.fakeSuccessCall(resp)
    }

    private fun fakePreset(toAdd: String) = PresetOperationsList.Preset(
        "1681217254000", "fakeNoticeNumber$toAdd", "fakeNoticeTaxCode$toAdd", "fakeOperationType$toAdd",
        "fakePaTaxCode$toAdd", "fakePresetId$toAdd", "fakeStatus$toAdd", PresetOperationsList.Preset.StatusDetails(
            "fakeAcquirerId$toAdd", "fakeChannel$toAdd", 100, "1681217254000", "fakeMerchantId$toAdd",
            listOf(oneNotice), "fakeStatus$toAdd", "fakeTerminalId$toAdd", 10000, "fakeTransactionId$toAdd"
        ), "1681217254000", "fakeSubscriberId$toAdd"
    )

    fun presetsListMocked() = liveData {
        val list = ArrayList<PresetOperationsList.Preset>()
        for (i in 0..5)
            list.add(fakePreset("$i"))
        this.fakeSuccessCall(PresetOperationsList(list.toList()))
    }

    fun lastPresetMocked() = liveData {
        this.fakeSuccessCall(fakePreset(""))
    }
}
