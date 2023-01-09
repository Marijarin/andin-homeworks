package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSignUpBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.AuthViewModel

class SignUpFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignUpBinding.inflate(inflater, container, false)



        binding.up.setOnClickListener {
            AndroidUtils.hideKeyboard(requireView())
            if (binding.username.text.isNotBlank() && binding.login.text.isNotBlank() && binding.password.text.isNotBlank() && binding.password.text.toString() == binding.confirm.text.toString()) {
                authViewModel.registerUser(binding.login.text.toString(),binding.password.text.toString(), binding.username.text.toString() )
            }
            if (binding.password.text.toString() != binding.confirm.text.toString()) {
                Snackbar.make(binding.root, R.string.not_confirmed, Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        authViewModel.state.observe(viewLifecycleOwner) {
            if (authViewModel.authenticated) {
                findNavController().navigateUp()
            }
        }

        return binding.root
    }
}