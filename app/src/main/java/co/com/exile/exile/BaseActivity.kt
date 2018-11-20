package co.com.exile.exile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.support.annotation.CallSuper
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import org.json.JSONException
import org.json.JSONObject

open class BaseActivity : AppCompatActivity() {

    protected var url: String? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val responseString = intent.getStringExtra("response")
            try {
                val response = JSONObject(responseString)
                when (response.getString("type")) {
                    "message" -> onMessage(response.getJSONObject("mensaje"))
                    "create_room_success" -> onRoomCreated(response.getJSONObject("room"))
                    "can_join_room" -> joinRoom(response.getJSONObject("room"))
                    "notification__room" -> onNotification(response)
                    "message_viewed" -> onMessageViewed(response)
                    "message_received" -> onMessageReceived(response)
                    "messages_status_updated" -> onStatusUpdated(response)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
    }

    protected open fun onMessage(message: JSONObject) {
        readMessage(message)
    }

    protected open fun onNotification(notification: JSONObject) {
        val  mBuilder = NotificationCompat.Builder(this, NotificationService.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_x)
                .setContentTitle("Nuevo Mensaje")
                .setContentText(notification.getString("message"))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)

        mBuilder.color = ContextCompat.getColor(this, R.color.colorPrimary)
        mBuilder.setVibrate(longArrayOf(0, 400, 100, 400))
        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, mBuilder.build())
        }
    }

    protected open fun onRoomCreated(message: JSONObject) = Unit

    @CallSuper
    protected open fun joinRoom(room: JSONObject) {
        sendCommandToService(JSONObject().apply {
            put("command", "join_room")
            put("room", room.getString("id"))
        })
    }

    protected open fun onMessageViewed(response: JSONObject) = Unit

    protected open fun onMessageReceived(response: JSONObject) = Unit

    protected open fun onStatusUpdated(response: JSONObject) = Unit

    protected fun readMessage(message: JSONObject){
        sendCommandToService(JSONObject().apply {
            put("command", "message_received")
            put("messageId", message.getString("messageId"))
        })
    }

    override fun onResume() {
        super.onResume()

        if (getURL() == null) {
            startActivityForResult(Intent(this, UrlActivity::class.java), 1)
        } else {
            url = getURL()
        }

        Log.e("tales5", "url: $url")

        val intentFilter = IntentFilter(ACTION_STRING_ACTIVITY)
        registerReceiver(receiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        if (getURL() == null) {
            startActivityForResult(Intent(this, UrlActivity::class.java), 1)
        } else {
            url = getURL()
        }

        Log.e("tales5", "url: $url")
    }

    private fun getURL(): String? {
        val sharedPref = getSharedPreferences("UrlPref", Context.MODE_PRIVATE)
        return sharedPref.getString("url", null)
    }

    protected fun getUrl(serviceUrl: String): String {
        var serviceUrl = serviceUrl
        if (serviceUrl.substring(0, 1) == "/") {
            serviceUrl = serviceUrl.substring(1)
        }
        return Uri.parse(getURL())
                .buildUpon()
                .appendEncodedPath(serviceUrl)
                .build()
                .toString()
    }

    fun sendCommandToService(command: JSONObject) {
        val intent = Intent()
        intent.action = ACTION_TO_SERVICE
        intent.putExtra("command", command.toString())
        sendBroadcast(intent)
    }

    companion object {
        private const val ACTION_TO_SERVICE = "ToService"
        private const val ACTION_STRING_ACTIVITY = "ToActivity"
    }
}
