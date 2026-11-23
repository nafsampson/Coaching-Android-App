// Defines translated and plain-text messages shown by the app.

package com.formup.app.ui

import androidx.annotation.StringRes

sealed interface UiMessage {
    data class Resource(
        @StringRes val resourceId: Int,
        val arguments: List<Any> = emptyList()
    ) : UiMessage

    data class Raw(val text: String) : UiMessage
}

internal interface MessageSink {
    fun notify(text: String)
    fun notify(@StringRes resourceId: Int, vararg args: Any)
}
