package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.AuthViewModel

class SignInFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()
    private var fragmentSignInBinding: FragmentSignInBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignInBinding.inflate(inflater, container, false)
        fragmentSignInBinding = binding


        binding.`in`.setOnClickListener {
            AndroidUtils.hideKeyboard(requireView())
            if (binding.login.text.isNotBlank() && binding.password.text.isNotBlank()){
                authViewModel.updateUser(binding.login.text.toString(),binding.password.text.toString() )
                }
        }

        authViewModel.state.observe(viewLifecycleOwner) {
            if (authViewModel.authenticated){
                findNavController().navigateUp()
            }
        }

        return binding.root
    }
}