package it.pagopa.swc.smartpos.idpay.model.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TransactionDetailResponse(
    @SerializedName("idpayTransactionId")
    val idpayTransactionId:String?,
    @SerializedName("milTransactionId")
    val milTransactionId:String?,
    @SerializedName("initiativeId")
    val initiativeId: String?,
    @SerializedName("timestamp")
    val timestamp: String?,
    @SerializedName("goodsCost")
    val goodsCost: Long?,
    @SerializedName("trxCode")
    val trxCode: String?,
    @SerializedName("coveredAmount")
    val coveredAmount: Long?,
    @SerializedName("secondFactor")
    val secondFactor: String?,
    @SerializedName("status")
    val status: String?
) : Serializable

enum class TransactionStatus : Serializable {
    CREATED,
    IDENTIFIED,
    AUTHORIZED,
    REJECTED,
    REWARDED,
    CANCELLED
}