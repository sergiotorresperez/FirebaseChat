package com.garrapeta.firebasechat

import java.util.*


data class ChatMessage(
    val messageText: String? = null,
    val messageUser: String? = null,
    val messageTime: Long = Date().time
)