package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                if (!post.likedByMe) viewModel.likeById(post.id) else if (post.likedByMe) viewModel.unlikeById(
                    post.id
                )
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onImage(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_imageFragment, Bundle().apply {
                        textArg = post.attachment?.url
                    })
            }
        })
        binding.newerPosts.visibility = View.GONE

        binding.list.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.emptyText.isVisible = state.empty

        }
        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state is FeedModelState.Loading
            binding.contentView.isRefreshing = state is FeedModelState.Refreshing
            if (state is FeedModelState.Error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.refresh() }
                    .show()
            }
        }
        viewModel.edited.observe(viewLifecycleOwner) { post ->
            if (post.id == 0L) {
                return@observe
            }
            findNavController()
                .navigate(R.id.action_feedFragment_to_newPostFragment, Bundle().apply {
                    textArg = post.content
                })

        }

        viewModel.newerCount.observe(viewLifecycleOwner) {
            println("Newer count ** $it")
            if (it > 0) {
                binding.newerPosts.isVisible = true
            } else binding.newerPosts.visibility = View.GONE
        }

        binding.newerPosts.setOnClickListener {
            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (positionStart == 0) {
                        binding.list.smoothScrollToPosition(0)
                    }
                }
            })
            viewModel.update()
            binding.newerPosts.isGone = true
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }
        binding.contentView.setOnRefreshListener {
            viewModel.refresh()
        }

        val authViewModel: AuthViewModel by viewModels()
        var menuProvider: MenuProvider? = null

        authViewModel.state.observe(viewLifecycleOwner){
            menuProvider?.let(requireActivity()::removeMenuProvider)
        }

        requireActivity().addMenuProvider(object: MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_auth, menu)

                menu.setGroupVisible(R.id.authenticated, authViewModel.authenticated)
                menu.setGroupVisible(R.id.unauthenticated, !authViewModel.authenticated)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.signout -> {
                        AppAuth.getInstance().removeAuth()
                        true
                    }
                    R.id.signin -> {
                        true
                    }
                    R.id.signup -> {
                        true
                    }
                    else -> false
                }
        }.apply {
                menuProvider = this
        }, viewLifecycleOwner)

        return binding.root
    }

}
