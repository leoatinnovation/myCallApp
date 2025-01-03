package com.example.mycallapp.services

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.example.mycallapp.commons.SHARED_PREF_NAME
import com.example.mycallapp.commons.SHARED_PREF_URI_NAME
import com.example.mycallapp.commons.SPAM_DISPLAY_NAME
import com.example.mycallapp.commons.SPAM_PHONE_CALL_NUMBER_94985
import com.example.mycallapp.commons.SPAM_PHONE_CALL_NUMBER_94986
import com.example.mycallapp.commons.SPAM_PHONE_CALL_NUMBER_JIO
import com.example.mycallapp.commons.SPAM_PHONE_CALL_NUMBER_SIM1981
import com.example.mycallapp.commons.events.MessageEvent
import com.example.mycallapp.commons.extensions.parseCountryCode
import com.example.mycallapp.commons.extensions.removeCountryPrefix
import com.example.mycallapp.commons.extensions.removeTelPrefix
import com.example.mycallapp.commons.utils.NotificationManagerImpl
import com.example.mycallapp.commons.utils.Utils
import org.greenrobot.eventbus.EventBus

class MyCallScreeningService : CallScreeningService() {

    private lateinit var notificationChannel: NotificationChannel
    private lateinit var notificationMgr: NotificationManager
    private lateinit var notificationBuilder: Notification.Builder

    private val CHANNEL_ID = "myCallAppId"

    // Request code for creating a PDF document.
    private val CREATE_FILE = 1

    private val NOTIFICATION_ID = 987654321

    private val notificationManager = NotificationManagerImpl()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = getPhoneNumber(callDetails)
        var displayName = getDisplayName(callDetails)
        var response = CallResponse.Builder()
        response = handlePhoneCall(response, phoneNumber, displayName)

        Log.d("SARA","ENd of onScreenCall >>>>>>>>>>>>>>>>>>")
        respondToCall(callDetails, response.build())
    }

    private fun handlePhoneCall(
        response: CallResponse.Builder,
        phoneNumber: String,
        displayName: String?
    ): CallResponse.Builder {
        Log.d("SARA","Handle PhoneCall >>>>>>>>>>>>>>>>>>")
        displayToast(String.format("SARA call from %s", phoneNumber))
        if((phoneNumber.startsWith(SPAM_PHONE_CALL_NUMBER_94985)) ||
                    (phoneNumber.startsWith(SPAM_PHONE_CALL_NUMBER_JIO)) ||
                    (phoneNumber.startsWith(SPAM_PHONE_CALL_NUMBER_SIM1981)) ||
                    (phoneNumber.startsWith(SPAM_PHONE_CALL_NUMBER_94986))) {
            displayToast(String.format("SPAM <><><><><>>> Rejected call from %s", phoneNumber))
            showNotification(phoneNumber)
            response.apply {
                setRejectCall(true)
                setDisallowCall(true)
                setSkipCallLog(false)
            }
            logtoFile(phoneNumber)
        } else if((displayName != null) && (displayName.contains(SPAM_DISPLAY_NAME))){
            displayToast(String.format("SPAM <><><><><>>> Rejected call from %s", displayName))
            showNotification(phoneNumber)
            response.apply {
                setRejectCall(true)
                setDisallowCall(true)
                setSkipCallLog(false)
            }
            logtoFile(phoneNumber)
        }
        else {
            displayToast(String.format(" SARA >>>>> Incoming call from %s", phoneNumber))
        }
        return response
    }

    private fun logtoFile(phoneNumber: String) {
        val sharedPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        var fileUri = sharedPref.getString(SHARED_PREF_URI_NAME,"")
        if (fileUri != null) {
            Utils.writeFileExternalStorage(applicationContext,fileUri, phoneNumber)
        }
    }

    private fun getPhoneNumber(callDetails: Call.Details): String {
        return callDetails.handle.toString().removeTelPrefix().parseCountryCode().removeCountryPrefix()
    }

    private fun getDisplayName(callDetails: Call.Details): String? {
        displayToast(String.format(" SARA getDisplayName callDetails.callerDisplayName - %s, callDetails.contactDisplayName - %s", callDetails.callerDisplayName, callDetails.contactDisplayName))
        var displayName = callDetails.contactDisplayName

        if(displayName.isNullOrEmpty()){
            displayName = callDetails.callerDisplayName
        }
        return displayName
    }

    private fun displayToast(message: String) {
        notificationManager.showToastNotification(applicationContext, message)
        EventBus.getDefault().post(MessageEvent(message))
    }
    private fun createNotificationChannel() {
        notificationMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "SPAM CALLS",
                NotificationManager.IMPORTANCE_DEFAULT
            ).also {
                it.enableLights(true)
                it.enableVibration(true)
            }
            notificationMgr.createNotificationChannel(notificationChannel)
        }
    }

    private fun showNotification(phoneNumber :String){
        notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("SPAM ALERT")
                .setContentText(String.format("SPAM Rejected call from %s", phoneNumber))
                .setSmallIcon(R.drawable.star_on)
        } else {
            Notification.Builder(this)
                .setContentTitle("SPAM ALERT")
                .setContentText(String.format("SPAM Rejected call from %s", phoneNumber))
                .setSmallIcon(R.drawable.star_on)
        }
        notificationMgr.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
}