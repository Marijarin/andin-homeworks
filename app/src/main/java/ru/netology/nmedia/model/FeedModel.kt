package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false,
    )

sealed interface FeedModelState{
    object Error: FeedModelState
    object Refreshing: FeedModelState
    object Loading: FeedModelState
    object Idle: FeedModelState
}
