package co.com.exile.exile.chat

import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import org.json.JSONException
import org.json.JSONObject

import co.com.exile.exile.BaseFragment
import co.com.exile.exile.R

class ChatFragment : BaseFragment(), RoomsListAdapter.OnRoomClickListener {

    private var adapter = RoomsListAdapter()
    private lateinit var mSwipe: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater?.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val roomList = view.findViewById<RecyclerView>(R.id.rooms_list)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        roomList.layoutManager = layoutManager
        roomList.setHasFixedSize(true)
        roomList.adapter = adapter

        adapter.setListener(this)

        mSwipe = view.findViewById(R.id.swipe)
        mSwipe.isRefreshing = true
        mSwipe.setOnRefreshListener { requestChats() }

        requestChats()
    }

    override fun onFriendsResponse(response: JSONObject) {
        Log.e("taleschat", response.toString())
    }

    override fun onChatsResponse(response: JSONObject) {
        Log.e("taleschat", response.toString())
        try {
            adapter.setRooms(response.getJSONArray("rooms"))
            mSwipe.isRefreshing = false
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    override fun onClick(room: JSONObject) {
        val intent = Intent(this.context, ChatActivity::class.java).apply {
            putExtra("room", room.toString())
        }
        startActivity(intent)
    }
}
