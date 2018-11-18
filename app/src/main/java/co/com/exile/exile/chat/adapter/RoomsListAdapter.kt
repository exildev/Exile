package co.com.exile.exile.chat.adapter


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.com.exile.exile.R
import co.com.exile.exile.ext.java.util.toChatDate
import co.com.exile.exile.ext.java.util.toChatDateFormat
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.chat.view.*
import org.json.JSONException
import org.json.JSONObject

internal class RoomsListAdapter : RecyclerView.Adapter<RoomsListAdapter.ReportViewHolder>() {

    private var rooms = mutableListOf<JSONObject>()
    private var reportClickListener: OnRoomClickListener? = null
    private var url: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.chat, parent, false)
        return ReportViewHolder(view)
    }

    fun setRooms(rooms: MutableList<JSONObject>) {
        this.rooms = rooms
        notifyDataSetChanged()
    }

    fun setUrl(url: String) {
        this.url = url
        notifyDataSetChanged()
    }

    fun setListener(reportClickListener: OnRoomClickListener) {
        this.reportClickListener = reportClickListener
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        try {
            val room = rooms[position]
            val me = room.getString("me")
            val member = room.getJSONArray("miembros")?.getJSONObject(0)
            val user =  "${member?.getString("nombre")} ${member?.getString("apellidos")}"
            val messages = mutableListOf<JSONObject>()

            room.getJSONArray("mensajes")?.let {
                for (i in 0 until it.length()) {
                    messages.add(it.getJSONObject(i))
                }
            }

            val lastMessage = messages.lastOrNull()
            val unreadNumber = try {
                room.getJSONArray("notifications").length()
            } catch (e: Exception) {
                0
            }
            val dateLast = lastMessage?.getString("created_at")?.toChatDate()?.toChatDateFormat()
            val url =  "${this.url}${member?.getString("avatar")}"

            holder.itemView.apply {
                this.user.text = user
                this.last_message.text = lastMessage?.getString("mensaje")
                this.unread_number.visibility = if (unreadNumber < 1) View.GONE else View.VISIBLE
                this.unread_bg.visibility = if (unreadNumber < 1) View.GONE else View.VISIBLE
                this.unread_number.text = "$unreadNumber"
                this.date_last.text = dateLast

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
        return rooms.size
    }

    internal interface OnRoomClickListener {
        fun onClick(room: JSONObject)
    }

    internal inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

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
