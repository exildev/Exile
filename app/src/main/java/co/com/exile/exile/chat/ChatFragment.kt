package co.com.exile.exile.chat

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import org.json.JSONException
import org.json.JSONObject

import co.com.exile.exile.BaseFragment
import co.com.exile.exile.R
import co.com.exile.exile.chat.adapter.RoomsListAdapter
import kotlinx.android.synthetic.main.fragment_chat.*
import org.json.JSONArray

class ChatFragment : BaseFragment(), RoomsListAdapter.OnRoomClickListener {

    private var adapter = RoomsListAdapter()
    private  var rooms: MutableList<JSONObject> = mutableListOf()
    private lateinit var friends: MutableList<JSONObject>


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater?.inflate(R.layout.fragment_chat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        roomList.layoutManager = layoutManager
        roomList.setHasFixedSize(true)
        roomList.adapter = adapter

        adapter.setListener(this)

        val swipeHandler = object : SwipeToDeleteCallback(context) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                showDeleteRoom(viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(roomList)

        swipe.isRefreshing = true
        swipe.setOnRefreshListener { requestChats() }

        requestChats()
        requestFriends()
    }

    override fun onResume() {
        super.onResume()
        requestChats()
        requestFriends()
        url?.let { adapter.setUrl(it) }
    }

    override fun onFriendsResponse(response: JSONObject) {
        this.friends = mutableListOf()
        val friends = response.getJSONArray("friends")
        for (i in 0 until friends.length()) {
            this.friends.add(i, friends.getJSONObject(i))
        }
    }

    override fun onChatsResponse(response: JSONObject) {
        try {
            val rooms = response.getJSONArray("rooms")
            this.rooms = mutableListOf()
            for (i in 0 until rooms.length()) {
                this.rooms.add(i, rooms.getJSONObject(i).apply { put("notifications", JSONArray(notifications.filter { it.getString("room") == this.getString("id") })) })
            }
            adapter.setRooms(this.rooms)
            swipe.isRefreshing = false
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    override fun onRoomDeleted(response: JSONObject) {
        val index = rooms.indexOfFirst { it.getString("id") == response.getString("room") }
        rooms.removeAt(index)
        adapter.notifyItemRemoved(index)
    }

    override fun onMessage(message: JSONObject) {
        super.onMessage(message)
        rooms.firstOrNull { it.getString("id") == message.getString("room") }?.let {
            it.getJSONArray("mensajes").put(message)
            adapter.notifyDataSetChanged()
        }
    }

    override fun joinRoom(room: JSONObject) {
        super.joinRoom(room)
        rooms.add(room)
        adapter.notifyDataSetChanged()
    }

    override fun onNotification(notification: JSONObject) {
        notifications.add(notification)
        val room = rooms.firstOrNull { it.getString("id") == notification.getString("room")}
        room?.put("notifications", JSONArray(notifications.filter { it.getString("room") == notification.getString("room") }))
        adapter.notifyDataSetChanged()
    }

    override fun onNotificationList(notifications: JSONArray) {
        this.notifications = mutableListOf()
        for (i in 0 until notifications.length()){
            Log.e("tales4", "noti ${notifications.getJSONObject(i)}")
            this.notifications.add(i, notifications.getJSONObject(i))
        }

        rooms.forEach {
            it.put("notifications", JSONArray(this.notifications.filter { it.getString("room") == it.getString("id") }))
        }
        adapter.notifyDataSetChanged()
    }

    override fun onClick(room: JSONObject) {
        val intent = Intent(this.context, ChatActivity::class.java).apply {
            putExtra("room", room.toString())
        }
        startActivity(intent)
    }

    private fun showDeleteRoom(position: Int) {
        AlertDialog.Builder(this.context).setMessage("¿Esta seguro que quiere borrar los mensajes?")
                .setCancelable(false)
                .setPositiveButton("Confirmar") { _, _ -> deleteRoom(position) }
                .setNegativeButton("Cancelar") { _, _ ->  adapter.notifyDataSetChanged() }
                .create()
                .show()
    }

    private fun deleteRoom(position: Int) {
        deleteChat(rooms[position].getString("id"))
    }

    fun addChat() {
        val intent = Intent(this.context, ChatFriendActivity::class.java).apply {
            putExtra("friends", JSONArray(friends).toString())
        }
        startActivity(intent)
    }
}
