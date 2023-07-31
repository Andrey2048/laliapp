package ru.netology.laliapp.dto

import java.time.Instant

data class Post(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val published: String,
    val coordinates: Coordinates? = null,
    val link: String? = null,
    val mentionIds: Set<Long> = emptySet(),
    val mentionedMe: Boolean = false,
    val likeOwnerIds: Set<Long> = emptySet(),
    val likedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
) {
    companion object {
        val emptyPost = Post(
            id = 0,
            authorId = 0,
            author = "",
            authorAvatar = "",
            content = "",
            published = Instant.now().toString(),
            mentionedMe = false,
            likedByMe = false,
        )
    }
}