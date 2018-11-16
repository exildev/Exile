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

class ChatFragment : BaseFragment(), RoomsListAdapter.OnRoomClickListener {

    private var adapter = RoomsListAdapter()
    private lateinit var rooms: MutableList<JSONObject>

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater?.inflate(R.layout.fragment_chat, container, false)
    }

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
    }

    override fun onFriendsResponse(response: JSONObject) {
        val intent = Intent(this.context, ChatFriendActivity::class.java).apply {
            putExtra("friends", response.getJSONArray("friends").toString())
        }
        startActivity(intent)
    }

    override fun onChatsResponse(response: JSONObject) {
        Log.e("taleschat", response.toString())
        try {
            val rooms = response.getJSONArray("rooms")
            this.rooms = mutableListOf()
            for (i in 0 until rooms.length()) {
                this.rooms.add(i, rooms.getJSONObject(i))
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

        //TODO: refrescar en caso de que sea necesario
        //requestChats()
        //swipe.isRefreshing = true
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
        requestFriends()
    }
}
