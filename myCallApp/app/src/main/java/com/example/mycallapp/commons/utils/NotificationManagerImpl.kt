package com.example.mycallapp.commons.utils

import android.content.Context
import android.os.Looper
import android.widget.Toast

/**
 * Interface that defines which methods will contain manager responsible for displaying notifications
 */
interface MyNotificationManager {


    /**
     * Displays push notification
     * @param context Context
     * @param message Toast message
     */
    fun showToastNotification(context: Context, message: String)

}

/**
 * Class that handles displaying notifications.
 */
class NotificationManagerImpl : MyNotificationManager {

    override fun showToastNotification(context: Context, message: String) {
        val t = Thread {
            try {
                Looper.prepare()
                Toast.makeText(context.applicationContext, message, Toast.LENGTH_LONG).show()
                Looper.loop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        t.start()
    }
}
