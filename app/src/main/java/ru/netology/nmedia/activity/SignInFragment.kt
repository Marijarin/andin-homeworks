package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.AuthViewModel

@AndroidEntryPoint
class SignInFragment : DialogFragment() {
    private val authViewModel: AuthViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignInBinding.inflate(inflater, container, false)

        binding.`in`.setOnClickListener {
            AndroidUtils.hideKeyboard(requireView())
            if (binding.login.text.isNotBlank() && binding.password.text.isNotBlank()) {
                authViewModel.updateUser(
                    binding.login.text.toString(),
                    binding.password.text.toString()
                )
            }
        }

        authViewModel.data.observe(viewLifecycleOwner) {
            if (authViewModel.authenticated) {
                findNavController().navigateUp()
                setFragmentResult("signInClosed", bundleOf())
            }
        }

        return binding.root
    }
}