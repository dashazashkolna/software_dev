package com.example.broadcastlogapp

data class BroadcastEvent(
    val id: String,
    val type: String,
    val timestamp: String,
    val details: String
)