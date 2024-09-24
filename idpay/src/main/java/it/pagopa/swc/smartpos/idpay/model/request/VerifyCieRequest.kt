package it.pagopa.swc.smartpos.idpay.model.request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class VerifyCieRequest(
    @SerializedName("nis") val nis: String = "",
    @SerializedName("ciePublicKey") val ciePublicKey: String = "",
    @SerializedName("sod") val sod: String = "",
    @SerializedName("signature") val signature: String = ""
) : Serializable