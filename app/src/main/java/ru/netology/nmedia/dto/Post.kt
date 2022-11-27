package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val attachment: Attachment?
    )

data class Attachment(
    val url: String,
    val description: String,
    val type: AttachmentType = AttachmentType.IMAGE
)
enum class AttachmentType {
    IMAGE
}

