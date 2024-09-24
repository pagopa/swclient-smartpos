package it.pagopa.swc_smartpos.network

import com.google.gson.annotations.SerializedName

data class EmptyResponse(@SerializedName("body") val body: String) : java.io.Serializable
