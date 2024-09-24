package it.pagopa.swc_smartpos.network.coroutines.utils

/**Data class to help us to observe LiveData from calls*/
data class Resource<out T>(val code: Int?, val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(code: Int?, data: T?): Resource<T> = Resource(code, Status.SUCCESS, data, null)
        fun <T> error(code: Int?, msg: String): Resource<T> = Resource(code, Status.ERROR, null, msg)
        fun <T> loading(): Resource<T> = Resource(null, Status.LOADING, null, null)
    }
}