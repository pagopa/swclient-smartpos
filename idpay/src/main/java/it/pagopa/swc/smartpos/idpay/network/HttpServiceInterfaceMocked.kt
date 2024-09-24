package it.pagopa.swc.smartpos.idpay.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataScope
import androidx.lifecycle.liveData
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import it.pagopa.readcie.nfc.Utils
import it.pagopa.swc.smartpos.app_shared.model.login.LoginRefreshTokenRequest
import it.pagopa.swc.smartpos.app_shared.model.login.LoginRequest
import it.pagopa.swc.smartpos.app_shared.model.login.LoginResponse
import it.pagopa.swc.smartpos.idpay.model.Initiatives
import it.pagopa.swc.smartpos.idpay.model.request.CreateTransactionRequest
import it.pagopa.swc.smartpos.idpay.model.response.CreateTransactionResponse
import it.pagopa.swc.smartpos.idpay.model.response.HistoricalTransactionsResponse
import it.pagopa.swc.smartpos.idpay.model.response.Transaction
import it.pagopa.swc.smartpos.idpay.model.response.TransactionDetailResponse
import it.pagopa.swc.smartpos.idpay.model.response.TransactionStatus
import it.pagopa.swc.smartpos.idpay.model.response.VerifyCieResponse
import it.pagopa.swc_smartpos.network.EmptyResponse
import it.pagopa.swc_smartpos.network.coroutines.utils.Resource
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import kotlinx.coroutines.delay
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class HttpServiceInterfaceMocked {
    private suspend fun <Obj> LiveDataScope<Resource<Obj>>.fakeSuccessCall(
        obj: Obj,
        code: Int = 200
    ) {
        emit(Resource.loading())
        delay(2000L)
        emit(Resource.success(code, obj))
    }

    private suspend infix fun <Obj> LiveDataScope<Resource<Obj>>.fakeErrorCallWithMessage(message: String) {
        emit(Resource.loading())
        delay(2000L)
        emit(Resource.error(500, message))
    }

    private suspend infix fun <Obj> LiveDataScope<Resource<Obj>>.fakeError400WithMessage(message: String) {
        emit(Resource.loading())
        delay(2000L)
        emit(Resource.error(400, message))
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

    private fun oneInitiative(kind: String) = Initiatives.InitiativeModel(
        UUID.randomUUID().toString(),
        if (kind == "BC") "Bonus cultura" else "Voucher connettività",
        if (kind == "BC") "Ministero dell'istruzione" else "Ministero delle telecomunicazioni"
    )

    fun fakeInitiativeList() = liveData {
        val arrayList = ArrayList<Initiatives.InitiativeModel>().apply {
            for (i in 0..10) {
                if (i % 2 == 0)
                    add(oneInitiative(""))
                else
                    add(oneInitiative("BC"))
            }
        }
        this.fakeSuccessCall(Initiatives(arrayList.toList()))
    }

    fun fakeCieVerify() = liveData {
        this.fakeSuccessCall(
            VerifyCieResponse(
                kty = "RSA",
                e = "AQAB",
                use = "enc",
                kid = "idpay-wrap-key-PAGOPA-33444433488-30390022/4545f71ad2c548d9b8f22ec3dde26488",
                exp = 1792403695,
                iat = 1706003695,
                n = "s5nlBwPCCWmp5DeXnOZIvHAkVxQ5uTLV0kmooPm8oWPi1ZAUWc03uOggBcknifv6219iiI83DQapMIOBfc2VJLDVC0QNLPzHvP8ifZlqMVLijp8CLDfruVXzPVqvlrXzVVjJM+dqBSVJlODI18+QNUWiYP2vHYo29t8hQne/dJEVQx9q+wFCE5j5hfLiB+Ms9twJgfIDYXmAYllWqY+vxL2gNs16N9TRA1JyxApNgJBbxb17z/ijEZKkqs/+eGsi/OtQA1v4uooM1XR1YswsspF1V5d+srCC9fX9+hQTIMUsB/XcTepABx36f7/10jOFPG2FMWroGUNAF9wneNpgQD8zNwXMwf85ElDp2k3BlqFcCTbNAXsFtIKHpI2GRbwLZoosfFqN3dB+3eRAIAkXBtIrzPVGdEg32Ob2DPirDU/KA3D776NCjD/fTyFpgApMkGMXkUFa3X5LMwIJBEvhy37lkLVdHz8j+CKUIPQrU1r4/UgnfY82wO7EyY5QoTtA3vU45VQs8DN4ObBHFOIrsAVYBlDagdnqKC0lIMlcTc6B+x9HwgTQS0X/YjdI3i/JcM2jA45IOZADr5iBLCVnDxOZYyPXImNR+GzAMQjcWpaGhNLzGl6FteaVx6WH250vmecvpTJo7/djB9/3GNnsInq0p4ptS2AKupN7vTIIXV0=",
                keyOps = listOf("wrapKey")
            )
        )
    }

    fun fakeDelete() = liveData {
        this.fakeSuccessCall(createOneTransaction(TransactionStatus.CANCELLED, 1000, 100, 1), 200)
    }

    fun fakeTransactionDetail() = liveData {
        val resp = TransactionDetailResponse(
            coveredAmount = 20000L,
            goodsCost = 25000L,
            milTransactionId = "fakeMilTransactionId",
            idpayTransactionId = "fakeIdPayTransactionId",
            initiativeId = "fakeInitiativeId",
            status = when (cntCallForFakePoll) {
                0 -> {
                    cntCallForFakePoll++
                    TransactionStatus.CREATED.name
                }

                1, 2 -> {
                    cntCallForFakePoll++
                    TransactionStatus.IDENTIFIED.name
                }

                else -> {
                    cntCallForFakePoll = 0
                    TransactionStatus.AUTHORIZED.name
                }
            },
            timestamp = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault()
            ).format(Date()),
            secondFactor = "5432101234567891",
            trxCode = "A7UG8GHI3"
        )
        WrapperLogger.i("TransactionStatus", resp.status.orEmpty())
        this.fakeSuccessCall(resp)
    }

    fun fakeCreateTransaction(req: CreateTransactionRequest) = liveData {
        //Mock to percentage here
        this.fakeSuccessCall(
            CreateTransactionResponse(
                challenge = Utils.bytesToString(
                    byteArrayOf(
                        0x00,
                        0x01,
                        0x02,
                        0x03,
                        0x04,
                        0x05,
                        0x06,
                        0x07
                    )
                ),
                goodsCost = req.goodsCost,
                milTransactionId = "fakeMilTransactionId",
                idpayTransactionId = "fakeIdPayTransactionId",
                initiativeId = "fakeInitiativeId",
                qrCode = "fakeQrCode",
                status = TransactionStatus.CREATED.name,
                timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(
                    Date()
                ),
                trxCode = "A7UG8GHI3",
                secondFactor = "483efab359c1",
                location = listOf("/mil-idpay/transactions/fakeMilTransactionId"),
                retryAfter = listOf(5),
                maxRetries = listOf(5)
            ), 201
        )
    }

    fun fakeAuthorize(): LiveData<Resource<EmptyResponse>> {
        return if (cntPinExhausted == 1) {
            cntPinExhausted = 3
            liveData {
                //Mock to percentage here
                this.fakeSuccessCall(EmptyResponse("Ok"), 200)
            }
        } else {
            cntPinExhausted--
            liveData {
                this fakeError400WithMessage Gson().toJson(
                    Error(
                        Error.ErrorBody(
                            listOf("00A000053"),
                            listOf("[00A000053] IDPay responds with WRONG_AUTH_CODE")
                        )
                    )
                )
            }
        }
    }

    private data class Error(@SerializedName("body") val body: ErrorBody) : Serializable {
        data class ErrorBody(
            @SerializedName("errors") val errors: List<String>,
            @SerializedName("descriptions") val descriptions: List<String>
        )
    }

    private fun createOneTransaction(
        status: TransactionStatus,
        coveredAmount: Long,
        goodsCost: Long,
        day: Int
    ): Transaction {
        val mDay = (1000 * 60 * 60 * 24) * day
        val mDayAfter = mDay + (1000 * 60 * 60 * 24) + 368000
        return Transaction(
            coveredAmount = coveredAmount,
            goodsCost = goodsCost,
            milTransactionId = "fakeMilTransactionId",
            idpayTransactionId = "fakeIdPayTransactionId",
            initiativeId = "fakeInitiativeId",
            status = status.name,
            trxCode = "A7UG8GHI3",
            timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(
                Date(
                    System.currentTimeMillis() - mDayAfter
                )
            ),
            lastUpdate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(
                Date(
                    System.currentTimeMillis() - mDayAfter
                )
            )
        )
    }

    private fun getHistoryList(): List<Transaction> {
        val back = ArrayList<Transaction>()
        for (i in 1 until 13) {
            val covered = (i * 10000).toLong()
            if (i % 3 == 0) {
                back.add(
                    createOneTransaction(
                        TransactionStatus.REJECTED,
                        (covered.toFloat() * 0.75f).toString().split(".")[0].toLong(),
                        covered,
                        i
                    )
                )
                continue
            }
            if (i % 2 == 0)
                back.add(createOneTransaction(TransactionStatus.AUTHORIZED, covered, covered, i))
            else
                back.add(
                    createOneTransaction(
                        TransactionStatus.CREATED,
                        (covered.toFloat() * 0.5f).toString().split(".")[0].toLong(),
                        covered,
                        i
                    )
                )
        }
        return back.toList()
    }

    fun fakeTransactionHistory() = liveData {
        this.fakeSuccessCall(
            HistoricalTransactionsResponse(getHistoryList())
        )
    }

    companion object {
        var cntCallForFakePoll = 0
        var cntPinExhausted = 3
    }
}
