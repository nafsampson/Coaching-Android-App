// Creates notification channels when the app starts.

package com.formup.app

import android.app.Application
import com.formup.app.notifications.NotificationHelper

class FormUpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
