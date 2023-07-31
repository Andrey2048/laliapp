package ru.netology.laliapp.dto

data class PushMessage(
    val recipientId: Long?,
    val content: String,
)