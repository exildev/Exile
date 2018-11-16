package co.com.exile.exile.chat.adapter


import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import org.json.JSONException
import org.json.JSONObject

import co.com.exile.exile.R
import co.com.exile.exile.ext.java.util.toChatDate
import co.com.exile.exile.ext.java.util.toChatDateFormat

internal class RoomsListAdapter : RecyclerView.Adapter<RoomsListAdapter.ReportViewHolder>() {

    private var rooms = mutableListOf<JSONObject>()
    private var reportClickListener: OnRoomClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.chat, parent, false)
        return ReportViewHolder(view)
    }

    fun setRooms(rooms: MutableList<JSONObject>) {
        this.rooms = rooms
        notifyDataSetChanged()
    }

    fun setListener(reportClickListener: OnRoomClickListener) {
        this.reportClickListener = reportClickListener
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        try {
            val room = rooms[position]
            Log.e("talesroom", room.toString())
            val me = room.getString("me")
            val member = room.getJSONArray("miembros")?.getJSONObject(0)
            val user =  "${member?.getString("nombre")} ${member?.getString("apellidos")}"
            val messages = mutableListOf<JSONObject>()

            room.getJSONArray("mensajes")?.let {
                for (i in 0 until it.length()) {
                    messages.add(it.getJSONObject(i))
                }
            }

            val lastMessage = messages.firstOrNull { it.getJSONObject("emisor").getString("id") != me }
            val unreadNumber = messages.filter { it.getJSONObject("emisor").getString("id") != me && !it.getBoolean("leido")}
            Log.e("tales pascuales", lastMessage?.getString("created_at")?.toChatDate().toString())
            val dateLast = lastMessage?.getString("created_at")?.toChatDate()?.toChatDateFormat()

            holder.apply {
                this.user.text = user
                this.lastMessage.text = lastMessage?.getString("mensaje")
                this.unreadNumber.visibility = if (unreadNumber.isEmpty()) View.GONE else View.VISIBLE
                this.unreadNumber.text = "${unreadNumber.size}"
                this.dateLast.text = dateLast
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    override fun getItemCount(): Int {
        return rooms.size
    }

    internal interface OnRoomClickListener {
        fun onClick(room: JSONObject)
    }

    internal inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var user: TextView = itemView.findViewById(R.id.user)
        var lastMessage: TextView = itemView.findViewById(R.id.last_message)
        var unreadNumber: TextView = itemView.findViewById(R.id.unread_number)
        var dateLast: TextView = itemView.findViewById(R.id.date_last)
        var avatar: ImageView = itemView.findViewById(R.id.avatar)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            try {
                reportClickListener?.onClick(rooms[adapterPosition])
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
}
