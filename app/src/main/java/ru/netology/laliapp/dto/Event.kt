package ru.netology.laliapp.dto

import ru.netology.laliapp.enumeration.EventType
import java.time.Instant

data class Event(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val datetime: String,
    val published: String,
    val coords: Coordinates? = null,
    val type: EventType,
    val likeOwnerIds: Set<Long> = emptySet(),
    val likedByMe: Boolean = false,
    val speakerIds: Set<Long> = emptySet(),
    val participantsIds: Set<Long> = emptySet(),
    val participatedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false,
) {
    companion object {
        val emptyEvent = Event(
            id = 0,
            authorId = 0,
            author = "",
            authorAvatar = "",
            content = "",
            published = Instant.now().toString(),
            datetime = Instant.now().toString(),
            type = EventType.ONLINE,
            speakerIds = emptySet(),
        )
    }
}