package co.com.exile.exile.chat.adapter

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import org.json.JSONException
import org.json.JSONObject

import co.com.exile.exile.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.chat.view.*

internal class FriendListAdapter : RecyclerView.Adapter<FriendListAdapter.ReportViewHolder>() {

    private var friend = mutableListOf<JSONObject>()
    private var friendClickListener: OnFriendClickListener? = null
    private var url: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.chat, parent, false)
        return ReportViewHolder(view)
    }

    fun setFriends(friends: MutableList<JSONObject>) {
        this.friend = friends
        notifyDataSetChanged()
    }

    fun setUrl(url: String) {
        this.url = url
        notifyDataSetChanged()
    }

    fun setListener(friendClickListener: OnFriendClickListener) {
        this.friendClickListener = friendClickListener
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        try {
            val friend = friend[position]
            Log.e("friend", friend.toString())

            val user =  "${friend.getString("nombre")} ${friend.getString("apellidos")}"

            val url =  "${this.url}${friend.getString("avatar")}"

            holder.itemView.apply {
                this.user.text = user
                Picasso.with(context)
                        .load(url)
                        .placeholder(R.drawable.default_avatar)
                        .into(avatar)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    override fun getItemCount(): Int {
        return friend.size
    }

    internal interface OnFriendClickListener {
        fun onClick(friend: JSONObject)
    }

    internal inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.apply {
                setOnClickListener(this@ReportViewHolder)

                last_message.visibility = View.GONE
                unread_number.visibility = View.GONE
                unread_bg.visibility = View.GONE
                date_last.visibility = View.GONE
            }

        }

        override fun onClick(view: View) {
            try {
                friendClickListener?.onClick(friend[adapterPosition])
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
}
