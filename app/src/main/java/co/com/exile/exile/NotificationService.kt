package co.com.exile.exile

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import org.json.JSONException
import org.json.JSONObject

class NotificationService : Service() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val responseString = intent.getStringExtra("response")
            try {
                val response = JSONObject(responseString)
                when (response.getString("type")) {
                    "message" -> onMessage(response.getJSONObject("mensaje"))
                    "can_join_room" -> joinRoom(response.getJSONObject("room"))
                    "notification__room" -> onNotification(response)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intentFilter = IntentFilter(ACTION_STRING_ACTIVITY)
        registerReceiver(receiver, intentFilter)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun joinRoom(room: JSONObject) {
        sendCommandToService(JSONObject().apply {
            put("command", "join_room")
            put("room", room.getString("id"))
        })
    }

    private fun readMessage(message: JSONObject){
        sendCommandToService(JSONObject().apply {
            put("command", "message_received")
            put("messageId", message.getString("messageId"))
        })
    }

    private fun sendCommandToService(command: JSONObject) {
        val intent = Intent()
        intent.action = ACTION_TO_SERVICE
        intent.putExtra("command", command.toString())
        sendBroadcast(intent)
    }

    private fun onMessage(message: JSONObject) {
        readMessage(message)
    }

    private fun onNotification(notification: JSONObject) {
        val  mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
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

    companion object {
        private const val ACTION_TO_SERVICE = "ToService"
        private const val ACTION_STRING_ACTIVITY = "ToActivity"
        private const val CHANNEL_ID = "ExileChannel"
    }
}
