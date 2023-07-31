package ru.netology.laliapp.dto

import ru.netology.laliapp.enumeration.AttachmentType

data class Attachment(
    val url: String,
    val type: AttachmentType,
)