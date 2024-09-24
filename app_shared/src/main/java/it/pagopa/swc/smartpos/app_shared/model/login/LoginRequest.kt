package it.pagopa.swc.smartpos.app_shared.model.login


import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("client_id")
    val clientId: String,
    @SerializedName("grant_type")
    val grantType: String,
    @SerializedName("scope")
    val scope: String
) : java.io.Serializable