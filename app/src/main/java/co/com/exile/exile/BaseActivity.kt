package co.com.exile.exile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.support.annotation.CallSuper
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
        //TODO: put the notifications logic
    }

    protected open fun onRoomCreated(message: JSONObject) = Unit

    @CallSuper
    protected open fun joinRoom(room: JSONObject) {
        sendCommandToService(JSONObject().apply {
            put("command", "join_room")
            put("room", room.getString("id"))
        })
    }

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
