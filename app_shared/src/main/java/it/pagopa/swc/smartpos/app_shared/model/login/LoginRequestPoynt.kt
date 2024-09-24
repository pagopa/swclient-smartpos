package it.pagopa.swc.smartpos.app_shared.model.login

import com.google.gson.annotations.SerializedName

data class LoginRequestPoynt(
    @SerializedName("ext_token")
    val extToken: String,
    @SerializedName("add_data")
    val addData: String,
    @SerializedName("client_id")
    val clientId: String,
    @SerializedName("grant_type")
    val grantType: String,
    @SerializedName("scope")
    val scope: String
) : java.io.Serializable
