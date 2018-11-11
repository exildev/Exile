package co.com.exile.exile.chat

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import co.com.exile.exile.BaseActivity
import co.com.exile.exile.R
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.content_chat.*
import org.json.JSONArray
import org.json.JSONObject

class ChatActivity : BaseActivity() {

    private lateinit var room: JSONObject
    private val adapter = MessageListAdapter()
    private lateinit var messages: MutableList<JSONObject>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        room = JSONObject(intent.getStringExtra("room"))
        Log.e("room", room.toString())
        val firstName = room.getJSONArray("miembros").getJSONObject(0).getString("nombre")
        val lastName = room.getJSONArray("miembros").getJSONObject(0).getString("apellidos")
        val messages = room.getJSONArray("mensajes")

        this.messages = mutableListOf()
        for (i in 0 until messages.length()) {
            this.messages.add(i, messages.getJSONObject(i))
        }

        toolbar.title = "$firstName $lastName"
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        messagesList.layoutManager = layoutManager
        messagesList.setHasFixedSize(true)
        messagesList.adapter = adapter

        adapter.setMe(room.getString("me"))
        adapter.setMessages(this.messages)
        messagesList.scrollToPosition(messages.length() - 1)

        fab.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val message = JSONObject().apply {
            put("command", "send")
            put("room", room.getString("id"))
            put("message", message_input.text.toString())
            put("miembros", JSONArray())
        }
        sendCommandToService(message)
        message_input.setText("")
    }

    override fun onMessage(message: JSONObject) {
        if (room.getString("id") == message.getString("room")) {
            messages.add(message)
            adapter.notifyDataSetChanged()
            messagesList.scrollToPosition(messages.size - 1)
        } else {
            super.onMessage(message)
        }
    }
}