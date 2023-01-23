package ru.netology.nmedia.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: PostViewModel by viewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentFeedBinding.inflate(inflater, container, false)
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(R.string.want_to_sign_in) { _, _ ->
                    findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
                }
                setNegativeButton(R.string.cancel) { _, _ ->
                    viewModel.refresh()
                }
            }
                .setIcon(R.drawable.ic_netology_48dp)
                .setMessage(R.string.warn_out)
            builder.create()
        }
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

            override fun onAuth() {
                alertDialog?.show()
            }
        }, appAuth)

        binding.list.adapter = adapter

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest(adapter::submitData)
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


        binding.fab.setOnClickListener {
            if (!authViewModel.authenticated) {
                alertDialog?.show()
            }
            setFragmentResultListener("signInClosed") { _, _ ->
                if (authViewModel.authenticated) findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            }
        }
        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest{
                it.refresh is LoadState.Loading
                        || it.append is LoadState.Loading
                        ||it.prepend is LoadState.Loading
            }
        }
        binding.contentView.setOnRefreshListener {
           adapter.refresh()
        }

        return binding.root
    }

}
