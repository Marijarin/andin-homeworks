package ru.netology.nmedia.dto

data class Media(val id: String)

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val attachment: Attachment?,
    val saved: Boolean,
    val ownedByMe: Boolean = false,
    val authorId: Long = 0L,
)

data class Attachment(
    val url: String,
    val type: AttachmentType = AttachmentType.IMAGE
)
enum class AttachmentType {
    IMAGE
}

