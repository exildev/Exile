package co.com.exile.exile.chat.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import org.json.JSONException

import co.com.exile.exile.R
import kotlinx.android.synthetic.main.chat_message.view.*
import org.json.JSONObject

internal class MessageListAdapter : RecyclerView.Adapter<MessageListAdapter.MessageViewHolder>() {

    private var messages = mutableListOf<JSONObject>()
    private var me = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.chat_message, parent, false)
        return MessageViewHolder(view)
    }

    fun setMessages(messages: MutableList<JSONObject>) {
        this.messages = messages
        notifyDataSetChanged()
    }

    fun setMe(me: String) {
        this.me = me
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        try {
            val message = messages[position]
            val prevEmmisorId = if (position > 0) messages[position - 1].getJSONObject("emisor")?.getString("id") else null
            val nextEmmisorId = if (position + 1 < messages.size) messages[position + 1].getJSONObject("emisor")?.getString("id") else null
            val emmisorId = message.getJSONObject("emisor")?.getString("id")
            val recibido = message.getBoolean("recibido")
            val leido = message.getBoolean("leido")

            if (emmisorId == me) {
                holder.itemView.apply {
                    (messageBg.layoutParams as? FrameLayout.LayoutParams)?.gravity = Gravity.END
                    avatar.visibility = View.GONE
                    message_me_constraint.visibility = View.VISIBLE
                    message_you_constraint.visibility = View.GONE
                    this.message.setTextColor(Color.WHITE)
                    read_status.visibility = View.GONE
                    if (!recibido) {
                        read_status.visibility = View.GONE
                    } else {
                        read_status.visibility = View.VISIBLE
                        if (leido) {
                            read_status.setImageResource(R.drawable.ic_done_all_white)
                        } else {
                            read_status.setImageResource(R.drawable.ic_done_white)
                        }
                    }
                }
            } else {
                holder.itemView.apply {
                    (messageBg.layoutParams as? FrameLayout.LayoutParams)?.gravity = Gravity.START
                    this.message.setTextColor(Color.BLACK)
                    message_me_constraint.visibility = View.GONE
                    message_you_constraint.visibility = View.VISIBLE
                }
            }

            val lastIndext = messages.indexOfLast {
                it.getJSONObject("emisor").getString("id") != me
            }

            holder.itemView.avatar.visibility = when {
                emmisorId != me && lastIndext == position -> View.VISIBLE
                emmisorId != me -> View.INVISIBLE
                else -> View.GONE
            }

            val background = when {
                emmisorId == me && prevEmmisorId == me && nextEmmisorId == me -> R.drawable.card_background_me_center
                emmisorId == me && prevEmmisorId != me && nextEmmisorId == me -> R.drawable.card_background_me_top
                emmisorId == me && prevEmmisorId == me && nextEmmisorId != me -> R.drawable.card_background_me_bottom
                emmisorId == me && prevEmmisorId != me && nextEmmisorId != me -> R.drawable.card_background_me_alone
                emmisorId != me && prevEmmisorId != me && nextEmmisorId != me -> R.drawable.card_background_you_center
                emmisorId != me && prevEmmisorId != me && (nextEmmisorId == me || nextEmmisorId == null) -> R.drawable.card_background_you_bottom
                emmisorId != me && (prevEmmisorId == me || prevEmmisorId == null) && nextEmmisorId != me && nextEmmisorId != null -> R.drawable.card_background_you_top
                emmisorId != me && prevEmmisorId == me && nextEmmisorId == me -> R.drawable.card_background_you_alone
                else -> R.drawable.card_background_you_alone
            }
            holder.itemView.messageBg.setBackgroundResource(background)
            holder.itemView.message.text = message.getString("mensaje")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    internal inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
