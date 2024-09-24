package it.pagopa.swc_smartpos.network

data class UserJson(val userId: Int, val id: Int, val title: String, val body: String) : java.io.Serializable
data class OtherUserJson(val ciao: String, val bye: String) : java.io.Serializable
data class UserJsonForHeader(val ciao: String, val bye: String, val headerVal: String) : java.io.Serializable