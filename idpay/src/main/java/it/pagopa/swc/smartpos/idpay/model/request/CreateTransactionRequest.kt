package it.pagopa.swc.smartpos.idpay.model.request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CreateTransactionRequest(
    @SerializedName("initiativeId")
    val initiativeId: String,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("goodsCost")
    val goodsCost: Long
) : Serializable
