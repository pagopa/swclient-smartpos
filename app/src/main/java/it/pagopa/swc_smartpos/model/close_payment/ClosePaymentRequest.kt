package it.pagopa.swc_smartpos.model.close_payment

import com.google.gson.annotations.SerializedName

data class ClosePaymentRequest(
    @SerializedName("outcome")
    val outcome: String,
    @SerializedName("paymentTimestamp")
    val paymentTimestamp: String,
    @SerializedName("paymentMethod")
    val paymentMethod: String= "PAYMENT_CARD"
) : java.io.Serializable