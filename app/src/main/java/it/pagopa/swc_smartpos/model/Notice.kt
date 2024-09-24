package it.pagopa.swc_smartpos.model

import com.google.gson.annotations.SerializedName

data class Notice(
    @SerializedName("amount")
    val amount: Int ? = null,
    @SerializedName("company")
    val company: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("noticeNumber")
    val noticeNumber: String? = null,
    @SerializedName("office")
    val office: String? = null,
    @SerializedName("paTaxCode")
    val paTaxCode: String? = null,
    @SerializedName("paymentToken")
    val paymentToken: String? = null,
) : java.io.Serializable