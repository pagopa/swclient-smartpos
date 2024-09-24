package it.pagopa.swc.smartpos.idpay.model.response


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CreateTransactionResponse(
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
    @SerializedName("challenge")
    val challenge: String?,//to pass to intAuth readCie lib
    @SerializedName("qrCode")
    val qrCode: String?,
    @SerializedName("trxCode")
    val trxCode: String?,
    @SerializedName("secondFactor")
    val secondFactor: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("Location")
    val location: List<String>?,
    @SerializedName("retry-after")
    val retryAfter: List<Int>?,
    @SerializedName("max-retries")
    val maxRetries: List<Int>?
) : Serializable