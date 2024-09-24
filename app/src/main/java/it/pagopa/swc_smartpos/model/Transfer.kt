package it.pagopa.swc_smartpos.model

import com.google.gson.annotations.SerializedName

data class Transfer(
    @SerializedName("paTaxCode")
    val paTaxCode: String,
    @SerializedName("category")
    val category: String
) : java.io.Serializable