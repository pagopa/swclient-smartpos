package it.pagopa.swc_smartpos.model.presets.subscribe


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SubscribeTerminalRequest(
    @SerializedName("label")
    val label: String,
    @SerializedName("paTaxCode")
    val paTaxCode: String
) : Serializable