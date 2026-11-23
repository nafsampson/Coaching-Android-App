// Creates system notifications and links them to app screens.

package com.formup.app.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.formup.app.MainActivity
import com.formup.app.R

private const val TAG = "FormUpNotify"

object NotificationHelper {

    const val CHANNEL_UPDATES = "formup_updates"
    const val CHANNEL_SYNC = "formup_sync"

    const val EXTRA_OWNER_UID = "formup_owner_uid"
    const val EXTRA_KIND = "formup_kind"
    const val EXTRA_FIXTURE_ID = "formup_fixture_id"
    const val EXTRA_NOTIFICATION_ID = "formup_notification_id"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_UPDATES, context.getString(R.string.loc_channel_updates), NotificationManager.IMPORTANCE_HIGH).apply {
                description = context.getString(R.string.loc_channel_updates_desc)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_SYNC, context.getString(R.string.loc_channel_sync), NotificationManager.IMPORTANCE_LOW).apply {
                description = context.getString(R.string.loc_channel_sync_desc)
            }
        )
        Log.d(TAG, "Notification channels created")
    }

    @SuppressLint("MissingPermission")                        
    fun showPush(context: Context, item: IncomingNotification, ownerUid: String) {
        if (!canPost(context)) {
            Log.w(TAG, "Notifications are disabled/not granted, skipping system notification for ${item.id}")
            return
        }
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OWNER_UID, ownerUid)
            putExtra(EXTRA_KIND, item.kind.name)
            putExtra(EXTRA_FIXTURE_ID, item.fixtureId)
            putExtra(EXTRA_NOTIFICATION_ID, item.id)
        }
        val pending = PendingIntent.getActivity(
            context,
            item.id.hashCode(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_UPDATES)
            .setSmallIcon(R.drawable.ic_stat_formup)
            .setContentTitle(item.title)
            .setContentText(item.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(item.body))
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        NotificationManagerCompat.from(context).notify(item.id.hashCode(), notification)
        Log.i(TAG, "Displayed account-owned notification")
    }

    @SuppressLint("MissingPermission")                        
    fun showSyncComplete(context: Context, count: Int) {
        if (!canPost(context)) return
        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC)
            .setSmallIcon(R.drawable.ic_stat_formup)
            .setContentTitle(context.getString(R.string.loc_back_online))
            .setContentText(
                if (count == 1) context.getString(R.string.loc_sync_one)
                else context.getString(R.string.loc_sync_many, count)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(SYNC_NOTIFICATION_ID, notification)
    }

    private fun canPost(context: Context) = NotificationManagerCompat.from(context).areNotificationsEnabled()

    private const val SYNC_NOTIFICATION_ID = 9001
}
