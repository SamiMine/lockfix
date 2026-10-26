package com.customlockscreen

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListenerService : NotificationListenerService() {

    companion object {
        const val ACTION_NOTIFICATION_RECEIVED = "com.customlockscreen.NOTIFICATION_RECEIVED"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        val finalText = when {
            title.isNotBlank() && text.isNotBlank() -> "$title: $text"
            title.isNotBlank() -> title
            text.isNotBlank() -> text
            bigText.isNotBlank() -> bigText
            else -> "New notification"
        }

        val intent = Intent(ACTION_NOTIFICATION_RECEIVED).apply {
            putExtra("notification_text", finalText)
        }
        sendBroadcast(intent)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // optional
    }
}
