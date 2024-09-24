package it.pagopa.swc_smartpos.model.presets


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CreatePresetOperationRequest(
    @SerializedName("noticeNumber")
    val noticeNumber: String,
    @SerializedName("noticeTaxCode")
    val noticeTaxCode: String,
    @SerializedName("operationType")
    val operationType: String,
    @SerializedName("paTaxCode")
    val paTaxCode: String,
    @SerializedName("subscriberId")
    val subscriberId: String
) : Serializable