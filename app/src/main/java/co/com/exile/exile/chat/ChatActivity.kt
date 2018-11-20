package co.com.exile.exile.chat

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import co.com.exile.exile.BaseActivity
import co.com.exile.exile.R
import co.com.exile.exile.chat.adapter.MessageListAdapter
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.content_chat.*
import org.json.JSONArray
import org.json.JSONObject

class ChatActivity : BaseActivity() {

    private var room: JSONObject? = null
    private var friend: JSONObject? = null
    private val adapter = MessageListAdapter()
    private lateinit var messages: MutableList<JSONObject>

    private var messageToSend: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        this.messages = mutableListOf()

        intent.getStringExtra("room")?.let {roomString ->
            room = JSONObject(roomString)

            room?.let {
                val firstName = it.getJSONArray("miembros")?.getJSONObject(0)?.getString("nombre")
                val lastName = it.getJSONArray("miembros")?.getJSONObject(0)?.getString("apellidos")
                val messages = it.getJSONArray("mensajes")

                readNotifications(it.getJSONArray("notifications"))

                for (i in 0 until (messages?.length() ?: 0)) {
                    this.messages.add(i, messages.getJSONObject(i))
                }
                toolbar.title = "$firstName $lastName"

                messagesUpdateStatus()
            }
        } ?:  intent.getStringExtra("friend")?.let {friendString ->
            friend = JSONObject(friendString)
            friend?.let {
                val firstName = it.getString("nombre")
                val lastName = it.getString("apellidos")

                toolbar.title = "$firstName $lastName"
            }
        }

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        messagesList.layoutManager = layoutManager
        messagesList.setHasFixedSize(true)
        messagesList.adapter = adapter

        adapter.setMe(room?.getString("me") ?: "")
        adapter.setMessages(this.messages)
        messagesList.scrollToPosition(messages.size - 1)

        fab.setOnClickListener {
            sendMessage()
        }
    }

    private fun readNotifications(notifications: JSONArray) {
        val notificationIds = mutableListOf<String>()
        for (i in 0 until notifications.length()) {
            notificationIds.add(notifications.getJSONObject(i).getString("id"))
        }

        sendCommandToService(JSONObject().apply {
            put("command", "notificacion_read")
            put("notifications", JSONArray(notificationIds))
            put("type", 2)
        })
    }

    private fun sendMessage() {
        val message = JSONObject().apply {
            put("command", "send")
            put("message", message_input.text.toString())
            put("miembros", JSONArray())
        }
        if (room != null) {
            message.put("room", room?.getString("id"))
            sendCommandToService(message)
        } else {
            sendCommandToService(JSONObject().apply {
                put("command", "create_chat")
                put("grupo", false)
                put("miembros", JSONArray().apply { put(friend?.getString("id")) })
            })
            messageToSend = message
        }
        message_input.setText("")
    }

    private fun markAsRead(message: JSONObject) {
        if (room?.getString("me") != message.getJSONObject("emisor").getString("id")) {
            Log.e("tales5", "read marked")
            sendCommandToService(JSONObject().apply {
                put("command", "message_viewed")
                put("messageId", message.getString("messageId"))
            })
        }
    }

    private fun messagesUpdateStatus() {
        val messages = this.messages.filter { !it.getBoolean("recibido") || !it.getBoolean("leido")}.map { it.getString("messageId") }
        Log.e("tales5", "$messages")
        sendCommandToService(JSONObject().apply {
            put("command", "messages_update_status")
            put("roomId", room?.getString("id"))
            put("viewed", true)
            put("recived", true)
            put("messages", JSONArray(messages))
        })
    }

    override fun onMessage(message: JSONObject) {
        if (room?.getString("id") == message.getString("room")) {
            messages.add(message)
            adapter.notifyDataSetChanged()
            messagesList.scrollToPosition(messages.size - 1)
            if (room?.getString("me") != message.getJSONObject("emisor").getString("id")) {
                readMessage(message)
                markAsRead(message)
            }
        } else {
            super.onMessage(message)
        }
    }

    override fun onMessageViewed(response: JSONObject) {
        val i = messages.indexOfFirst { it.getString("messageId") == response.getString("messageId") }
        if (i >= 0) {
            messages[i].put("leido", true)
            messages[i].put("recibido", true)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onMessageReceived(response: JSONObject) {
        val i = messages.indexOfFirst { it.getString("messageId") == response.getString("messageId") }
        if (i >= 0) {
            messages[i].put("recibido", true)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onStatusUpdated(response: JSONObject) {
        Log.e("tales5", response.toString(4))
        val messageIds = response.getJSONArray("messages")
        for (i in 0 until messageIds.length()) {
            val id = messageIds.getString(i)

            val index = messages.indexOfFirst { it.getString("messageId") == id }
            if (index >= 0) {
                messages[i].put("leido", true)
                messages[i].put("recibido", true)
            }
        }

        adapter.notifyDataSetChanged()
    }

    override fun onNotification(notification: JSONObject) {
        if (room?.getString("id") == notification.getString("room")) {
            readNotifications(JSONArray().put(notification))
        } else {
            super.onNotification(notification)
        }
    }

    override fun onRoomCreated(message: JSONObject) {
        room = message
        friend = null

        adapter.setMe(message.getString("me"))

        messageToSend?.let {
            it.put("room", room?.getString("id"))
            sendCommandToService(it)
            messageToSend = null
        }
    }
}