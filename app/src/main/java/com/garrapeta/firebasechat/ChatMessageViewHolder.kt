package com.garrapeta.firebasechat

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val messageText : TextView = itemView.findViewById(R.id.message_text) as TextView
    val messageUser : TextView = itemView.findViewById(R.id.message_user) as TextView
    val messageTime : TextView = itemView.findViewById(R.id.message_time) as TextView

}