package ru.netology.nmedia.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSignUpBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.AuthViewModel

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignUpBinding.inflate(inflater, container, false)
        binding.avatar.visibility = View.INVISIBLE
        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        authViewModel.changePhoto(uri, uri?.toFile())
                    }
                }
            }

        binding.uploadAvatar.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .compress(512)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        authViewModel.photo.observe(viewLifecycleOwner) {
            binding.avatar.setImageURI(it.uri)
            binding.avatar.isVisible = it.uri != null
        }

        binding.up.setOnClickListener {
            AndroidUtils.hideKeyboard(requireView())
            if (
                binding.username.text.isNotBlank()
                && binding.login.text.isNotBlank()
                && binding.password.text.isNotBlank()
                && binding.password.text.toString() == binding.confirm.text.toString()
            ) {
                when (authViewModel.photo.value) {
                    null -> authViewModel.registerUser(
                        binding.login.text.toString(),
                        binding.password.text.toString(),
                        binding.username.text.toString()
                    )
                    else -> authViewModel.photo.value?.file?.let { file ->
                        authViewModel.registerWithPhoto(
                            binding.login.text.toString(),
                            binding.password.text.toString(),
                            binding.username.text.toString(),
                            file
                        )
                    }
                }

            }
            if (binding.password.text.toString() != binding.confirm.text.toString()) {
                Snackbar.make(binding.root, R.string.not_confirmed, Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        authViewModel.data.observe(viewLifecycleOwner) {
            if (authViewModel.authenticated) {
                findNavController().navigateUp()
            }
        }

        return binding.root
    }
}