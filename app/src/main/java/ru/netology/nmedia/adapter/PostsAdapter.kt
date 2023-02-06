package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun onImage(post: Post) {}
    fun onAuth() {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val appAuth: AppAuth
) : PagingDataAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    companion object {
        const val TODAY_POST_ID = 1
        const val YESTERDAY_POST_ID = 2
        const val LAST_WEEK_POST_ID = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener, appAuth)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position) ?: return
        holder.bind(post)
    }

    override fun getItemViewType(position: Int) =
        when (getItem(position)?.let { diffInDays(it) }!!) {
            in 0.0..0.01 -> TODAY_POST_ID
            in 0.01..0.015 -> YESTERDAY_POST_ID
            in 0.015..8.0 -> LAST_WEEK_POST_ID
            else -> 0
        }

    @OptIn(ExperimentalTime::class)
    fun diffInDays(post: Post): Double {
        val previousTimeStamp = post.published.toLong()
        val nextTimeStamp = System.currentTimeMillis() / 1000
        val difference = (nextTimeStamp - previousTimeStamp).toDouble()

        return Duration.convert(
            value = difference,
            sourceUnit = DurationUnit.SECONDS,
            targetUnit = DurationUnit.DAYS
        )
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
    private val appAuth: AppAuth
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
    }

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            // в адаптере
            like.isChecked = post.likedByMe
            like.text = "${post.likes}"

            Glide.with(avatar)
                .load("${BASE_URL}/avatars/${post.authorAvatar}")
                .circleCrop()
                .placeholder(R.drawable.ic_loading_24)
                .error(R.drawable.ic_error_24)
                .timeout(10_000)
                .into(avatar)

            attachment.let {
                if (post.attachment != null) {
                    Glide.with(attachment)
                        .load("${BASE_URL}/media/${post.attachment.url}")
                        .placeholder(R.drawable.ic_loading_24)
                        .error(R.drawable.ic_error_24)
                        .timeout(10_000)
                        .into(attachment)
                }
            }
            attachment.isVisible = post.attachment != null

            menu.isVisible = post.ownedByMe

            attachment.setOnClickListener { onInteractionListener.onImage(post) }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                if (appAuth.state.value.id != 0L) {
                    onInteractionListener.onLike(post)
                } else if (appAuth.state.value.id == 0L) {
                    like.isChecked = false
                    like.isEnabled = false
                    onInteractionListener.onAuth()

                }
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}
