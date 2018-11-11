package co.com.exile.exile.chat

import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import org.json.JSONArray
import org.json.JSONException

import co.com.exile.exile.R
import kotlinx.android.synthetic.main.chat_message.view.*

internal class MessageListAdapter : RecyclerView.Adapter<MessageListAdapter.MessageViewHolder>() {

    private var messages: JSONArray? = null
    private var me = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.chat_message, parent, false)
        return MessageViewHolder(view)
    }

    fun setMessages(messages: JSONArray) {
        this.messages = messages
        notifyDataSetChanged()
    }

    fun setMe(me: String) {
        this.me = me
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        try {
            val message = messages?.getJSONObject(position)
            val prevEmmisorId = if (position > 0) messages?.getJSONObject(position - 1)?.getJSONObject("emisor")?.getString("id") else null
            val nextEmmisorId = if (position + 1 < messages?.length()?: 0) messages?.getJSONObject(position + 1)?.getJSONObject("emisor")?.getString("id") else null
            val emmisorId = message?.getJSONObject("emisor")?.getString("id")

            if (emmisorId == me) {
                holder.itemView.apply {
                    (messageBg.layoutParams as? FrameLayout.LayoutParams)?.gravity = Gravity.END
                    avatar.visibility = View.GONE
                    message_me_constraint.visibility = View.VISIBLE
                    message_you_constraint.visibility = View.GONE

                }
            } else {
                holder.itemView.apply {
                    avatar.visibility = View.VISIBLE
                    message_me_constraint.visibility = View.GONE
                    message_you_constraint.visibility = View.VISIBLE
                }
            }

            val background = when {
                emmisorId == me && prevEmmisorId == me && nextEmmisorId == me -> R.drawable.card_background_me_center
                emmisorId == me && prevEmmisorId != me && nextEmmisorId == me -> R.drawable.card_background_me_top
                emmisorId == me && prevEmmisorId == me && nextEmmisorId != me -> R.drawable.card_background_me_bottom
                emmisorId == me && prevEmmisorId != me && nextEmmisorId != me -> R.drawable.card_background_me_alone
                emmisorId != me && prevEmmisorId != me && nextEmmisorId != me -> R.drawable.card_background_you_center
                emmisorId != me && prevEmmisorId != me && nextEmmisorId == me -> R.drawable.card_background_you_top
                emmisorId != me && prevEmmisorId == me && nextEmmisorId != me -> R.drawable.card_background_you_bottom
                emmisorId != me && prevEmmisorId == me && nextEmmisorId == me -> R.drawable.card_background_you_alone
                else -> R.drawable.card_background_you_alone
            }
            holder.itemView.messageBg.setBackgroundResource(background)
            holder.itemView.message.text = message?.getString("mensaje")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return messages?.length() ?: 0
    }

    internal inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
