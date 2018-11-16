package co.com.exile.exile.chat

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import co.com.exile.exile.BaseActivity
import co.com.exile.exile.R
import co.com.exile.exile.chat.adapter.FriendListAdapter

import kotlinx.android.synthetic.main.activity_chat_friend.*
import kotlinx.android.synthetic.main.content_chat_friend.*
import org.json.JSONArray
import org.json.JSONObject

class ChatFriendActivity : BaseActivity(), FriendListAdapter.OnFriendClickListener {

    private val friends = mutableListOf<JSONObject>()
    private val adapter = FriendListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_friend)

        val friends = JSONArray(intent.getStringExtra("friends"))
        for (i in 0 until friends.length()) {
            this.friends.add(i, friends.getJSONObject(i))
        }

        toolbar.subtitle = "${this.friends.size} Contactos"
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        friendList.layoutManager = layoutManager
        friendList.setHasFixedSize(true)
        friendList.adapter = adapter

        adapter.setListener(this)
        adapter.setFriends(this.friends)
    }

    override fun onResume() {
        super.onResume()

        url?.let { adapter.setUrl(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chat_friend_menu, menu)

        val searchView = menu.findItem(R.id.search).actionView as? SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.i("search", "SearchOnQueryTextSubmit: $query")
                if (!searchView.isIconified) {
                    searchView.isIconified = true
                }
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                filter(s)
                return false
            }
        })
        return true
    }

    override fun onClick(friend: JSONObject) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("friend", friend.toString())
        }
        startActivity(intent)
        finish()
    }

    private fun filter(search: String) {
        adapter.setFriends(friends.filter { "${it.getString("nombre")} ${it.getString("apellidos")}".toLowerCase().contains(search.toLowerCase()) }.toMutableList())
    }
}
