package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentImageBinding
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class ImageFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg

        private const val BASE_URL = "http://10.0.2.2:9999"
    }


    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

    private var imageFragmentBinding: FragmentImageBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentImageBinding.inflate(
            inflater,
            container,
            false
        )
        imageFragmentBinding = binding

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state is FeedModelState.Error) {
                Snackbar
                    .make(binding.root, R.string.error_loading, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.retry_loading) {
                        viewModel.save()
                    }
                    .setBackgroundTint(
                        ContextCompat.getColor(
                            this.requireContext(),
                            R.color.colorDark
                        )
                    )
                    .setTextColor(ContextCompat.getColor(this.requireContext(), R.color.colorLight))
                    .show()
            }
        }
        binding.backwards.setOnClickListener {
            findNavController().navigateUp()
        }

        Glide.with(binding.photo)
            .load("${BASE_URL}/media/${arguments?.textArg}")
            .placeholder(R.drawable.ic_loading_24)
            .error(R.drawable.ic_error_24)
            .timeout(10_000)
            .into(binding.photo)

        return binding.root
    }
}
