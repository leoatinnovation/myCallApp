
package com.example.mycallapp

//import com.example.mycallapp.R
import android.R
import android.R.attr.name
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import com.example.mycallapp.commons.SHARED_PREF_NAME
import com.example.mycallapp.commons.SHARED_PREF_URI_NAME
import com.example.mycallapp.commons.base.BaseActivity
import com.example.mycallapp.commons.base.NoticeDialogFragment
import com.example.mycallapp.commons.events.MessageEvent
import com.example.mycallapp.commons.events.PermissionDenied
import com.example.mycallapp.commons.events.PhoneManifestPermissionsEnabled
import com.example.mycallapp.commons.utils.CapabilitiesRequestorImpl
import com.example.mycallapp.commons.utils.ManifestPermissionRequesterImpl
import com.example.mycallapp.ui.theme.MyCallAppTheme
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pub.devrel.easypermissions.AppSettingsDialog
import java.lang.ref.WeakReference


class MainActivity : BaseActivity(),
    NoticeDialogFragment.NoticeDialogListener {

    private val manifestPermissionRequestor = ManifestPermissionRequesterImpl()

    private val capabilitiesRequestor = CapabilitiesRequestorImpl()

    private val CHANNEL_ID = "myCallAppId"

    private val NOTIFICATION_ID = 987654321

    private val REQUEST_ID_MULTIPLE_PERMISSIONS: Int = 1
    private val CREATE_FILE = 1

    private val FILE_REQUEST_CODE = 2

    private lateinit var notificationChannel: NotificationChannel
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: Notification.Builder

    // flag that restarts checking capabilities dialog, after user enables manifest permissions
    // via app settings page
    private var checkCapabilitiesOnResume = false

    var startActivityForResult: ActivityResultLauncher<Intent> =
        registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                result?.data?.also { resultData ->
                    val contentResolver = applicationContext.contentResolver
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    // Check for the freshest data.
                    contentResolver.takePersistableUriPermission(resultData.data.toString().toUri(), takeFlags)
                    saveFileUri(resultData.data.toString())
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyCallAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        listenUiEvents()
        createNotificationChannel()
        //
        manifestPermissionRequestor.activity = WeakReference(this)
        capabilitiesRequestor.activityReference = WeakReference(this)
        manifestPermissionRequestor.getPermissions()
        showNoticeDialog()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        if (checkCapabilitiesOnResume) {
            capabilitiesRequestor.invokeCapabilitiesRequest()
            checkCapabilitiesOnResume = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        manifestPermissionRequestor.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
            capabilitiesRequestor.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDialogPositiveClick() {
        createFile()
    }

    private fun listenUiEvents() {
        uiEvent.observe(this, {
            when (it) {
                is PermissionDenied -> {
                    checkCapabilitiesOnResume = true
                    // This will display a dialog directing them to enable the permission in app settings.
                    AppSettingsDialog.Builder(this).build().show()
                }
                is PhoneManifestPermissionsEnabled -> {
                    // now we can load phone dialer capabilities requests
                    capabilitiesRequestor.invokeCapabilitiesRequest()
                }
                else -> {
                    // NOOP
                }
            }
        })
    }
    private fun createNotificationChannel() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "SPAM CALLs",
                NotificationManager.IMPORTANCE_DEFAULT
            ).also {
                it.enableLights(true)
                it.enableVibration(true)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun showNotification(){
        notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("SPAM ALERT")
                .setContentText("SPAM call alert")
                .setSmallIcon(R.drawable.star_on)
        } else {
            Notification.Builder(this)
                .setContentTitle("SPAM ALERT")
                .setContentText("SPAM call alert")
                .setSmallIcon(R.drawable.star_on)
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

    }

    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/text"
            putExtra(Intent.EXTRA_TITLE, "SpamCalls.txt")

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
            // putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult.launch(intent)

    }

    private fun showNoticeDialog() {
        val dialog = NoticeDialogFragment()
        dialog.show(supportFragmentManager, "NoticeDialogFragment")
    }

    private fun saveFileUri(fileUri: String){
        val sharedPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString(SHARED_PREF_URI_NAME, fileUri)
            apply()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        Log.d("myCallApp","EVENT >>>>>>>>>>>>>>>>>>>>>")
        showNotification()
//        val newText = String.format("%s\n%s", event.message, textLog.text.toString())
//        textLog.setText(newText)
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

}

