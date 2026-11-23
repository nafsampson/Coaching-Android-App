// Keeps unconfirmed attendance marks separate from saved records.

package com.formup.app.ui

import com.formup.app.data.ApiValues

internal data class AttendanceDraft(val changes: Map<String, String> = emptyMap()) {
    fun stage(base: Map<String, String>, patch: Map<String, String>) = AttendanceDraft(
        (changes + patch).filter { (id, value) -> value != (base[id] ?: ApiValues.NOT_RECORDED) })
    // Apply only changed marks so other confirmed values are preserved.
    fun resolve(base: Map<String, String>) = base + changes
    val hasChanges: Boolean get() = changes.isNotEmpty()
}
