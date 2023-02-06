package ru.netology.nmedia.dto

sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val attachment: Attachment?,
    val ownedByMe: Boolean = false,
    val authorId: Long = 0L,
): FeedItem

data class Ad(
    override val id: Long,
    val image: String,
): FeedItem


data class Attachment(
    val url: String,
    val type: AttachmentType = AttachmentType.IMAGE
)
enum class AttachmentType {
    IMAGE
}
