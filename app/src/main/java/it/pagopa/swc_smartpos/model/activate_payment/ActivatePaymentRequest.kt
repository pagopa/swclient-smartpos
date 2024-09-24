package it.pagopa.swc_smartpos.model.activate_payment

import com.google.gson.annotations.SerializedName

data class ActivatePaymentRequest(
    @SerializedName("idempotencyKey")
    val idempotencyKey: String,
    @SerializedName("amount")
    val amount: Int
) : java.io.Serializable