package it.pagopa.swc.smartpos.idpay.model.response


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class VerifyCieResponse(
    @SerializedName("kty")
    val kty: String?,
    @SerializedName("e")
    val e: String?,
    @SerializedName("use")
    val use: String?,
    @SerializedName("kid")
    val kid: String?,
    @SerializedName("exp")
    val exp: Int?,
    @SerializedName("iat")
    val iat: Int?,
    @SerializedName("n")
    val n: String?,
    @SerializedName("keyOps")
    val keyOps: List<String>?
) : Serializable