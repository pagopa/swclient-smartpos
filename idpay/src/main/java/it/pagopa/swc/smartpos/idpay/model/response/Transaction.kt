package it.pagopa.swc.smartpos.idpay.model.response

import com.google.gson.annotations.SerializedName

data class Transaction(
    @SerializedName("idpayTransactionId")
    val idpayTransactionId: String?,
    @SerializedName("milTransactionId")
    val milTransactionId: String?,
    @SerializedName("initiativeId")
    val initiativeId: String?,
    @SerializedName("timestamp")
    val timestamp: String?,
    @SerializedName("lastUpdate")
    val lastUpdate: String?,
    @SerializedName("goodsCost")
    val goodsCost: Long?,
    @SerializedName("trxCode")
    val trxCode: String?,
    @SerializedName("coveredAmount")
    val coveredAmount: Long?,
    @SerializedName("status")
    val status: String?
) : java.io.Serializable