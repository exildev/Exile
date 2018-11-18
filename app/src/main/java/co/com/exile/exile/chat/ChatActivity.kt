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

                for (i in 0 until (messages?.length() ?: 0)) {
                    this.messages.add(i, messages.getJSONObject(i))
                }
                toolbar.title = "$firstName $lastName"
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

    override fun onMessage(message: JSONObject) {
        if (room?.getString("id") == message.getString("room")) {
            messages.add(message)
            adapter.notifyDataSetChanged()
            messagesList.scrollToPosition(messages.size - 1)
        } else {
            super.onMessage(message)
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