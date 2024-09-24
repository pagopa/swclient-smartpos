package it.pagopa.swc_smartpos.model.close_payment

import com.google.gson.annotations.SerializedName
import it.pagopa.swc_smartpos.model.Notice
import java.io.Serializable

data class ClosePaymentPollingResponse(
    @SerializedName("acquirerId")
    val acquirerId: String,
    @SerializedName("channel")
    val channel: String,
    @SerializedName("fee")
    val fee: Int,
    @SerializedName("insertTimestamp")
    val insertTimestamp: String,
    @SerializedName("merchantId")
    val merchantId: String,
    @SerializedName("notices")
    val notices: List<Notice>,
    @SerializedName("status")
    val status: String,
    @SerializedName("terminalId")
    val terminalId: String,
    @SerializedName("totalAmount")
    val totalAmount: Int,
    @SerializedName("transactionId")
    val transactionId: String
) : Serializable