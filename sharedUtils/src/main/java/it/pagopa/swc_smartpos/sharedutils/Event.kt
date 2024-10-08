package it.pagopa.swc_smartpos.sharedutils

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
class Event<out T>(private val content: T) {
    /** Allow external read but not write*/
    var hasBeenHandled = false
        private set

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}