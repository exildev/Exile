package co.com.exile.exile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.util.Log
import org.json.JSONArray

import org.json.JSONException
import org.json.JSONObject

open class BaseFragment : Fragment() {

    protected var url: String? = null

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val responseString = intent.getStringExtra("response")
            try {
                val response = JSONObject(responseString)
                when (response.getString("type")) {
                    "friends" -> onFriendsResponse(response)
                    "rooms" -> onChatsResponse(response)
                    "room_delete" -> onRoomDeleted(response)
                    "message" -> onMessage(response.getJSONObject("mensaje"))
                    "can_join_room" -> joinRoom(response.getJSONObject("room"))
                    "notification__room" -> onNotification(response)
                    "notification" -> onNotificationList(response.getJSONArray("notifications_room"))
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
    }

    override fun onResume() {
        super.onResume()

        if (getURL() == null) {
            startActivityForResult(Intent(this.context, UrlActivity::class.java), 1)
        } else {
            url = getURL()
        }

        Log.e("tales5", "url: $url" )

        val intentFilter = IntentFilter(ACTION_STRING_ACTIVITY)
        context.registerReceiver(receiver, intentFilter)

        requestNotifications()
    }

    override fun onPause() {
        super.onPause()
        context.unregisterReceiver(receiver)
    }

    private fun getURL(): String? {
        val sharedPref = activity.getSharedPreferences("UrlPref", Context.MODE_PRIVATE)
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

    protected fun requestFriends() {
        try {
            sendCommandToService(JSONObject().put("command", "friends"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    protected fun requestChats() {
        try {
            sendCommandToService(JSONObject().put("command", "rooms"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    protected fun deleteChat(roomId: String) {
        try {
            sendCommandToService(JSONObject().apply {
                put("command", "delete_room")
                put("room", roomId)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected fun requestNotifications() {
        try {
            sendCommandToService(JSONObject().put("command", "notification"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    protected open fun onFriendsResponse(response: JSONObject) = Unit

    protected open fun onChatsResponse(response: JSONObject) = Unit

    protected open fun onRoomDeleted(response: JSONObject) = Unit

    protected open fun onMessage(message: JSONObject) = Unit

    @CallSuper
    protected open fun joinRoom(room: JSONObject) {
        sendCommandToService(JSONObject().apply {
            put("command", "join_room")
            put("room", room.getString("id"))
        })
    }

    protected open fun onNotification(notification: JSONObject) {
        //TODO: put the notifications logic
    }

    protected open fun onNotificationList(notifications: JSONArray) {
        //TODO: agregar notificaciones al bottom bar
    }

    private fun sendCommandToService(command: JSONObject) {
        val intent = Intent()
        intent.action = ACTION_TO_SERVICE
        intent.putExtra("command", command.toString())
        activity.sendBroadcast(intent)
    }

    companion object {
        private const val ACTION_TO_SERVICE = "ToService"
        private const val ACTION_STRING_ACTIVITY = "ToActivity"
    }
}
