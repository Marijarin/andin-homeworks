package ru.netology.nmedia.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.viewmodel.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AppActivity : AppCompatActivity(R.layout.activity_app) {
    private val authViewModel: AuthViewModel by viewModels()

    private var menuProvider: MenuProvider? = null

    @Inject
    lateinit var appAuth: AppAuth
    @Inject
    lateinit var firebaseMessaging: FirebaseMessaging
    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val alertDialog: AlertDialog? = let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(
                    R.string.sign_out
                ) { _, _ ->
                    appAuth.removeAuth()
                    findNavController(R.id.nav_host_fragment).navigateUp()
                }
            }
                .setIcon(R.drawable.ic_netology_48dp)
                .setMessage(R.string.want_to_sign_out)
            builder.create()
        }
        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                return@let
            }

            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment)
                .navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = text
                    }
                )
        }

        checkGoogleApiAvailability()

        authViewModel.data.observe(this) {
            menuProvider?.also(::removeMenuProvider) // Здесь тоже не забываем, иначе меню дублироваться будет
            addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_auth, menu)
                    menu.setGroupVisible(R.id.authenticated, authViewModel.authenticated)
                    menu.setGroupVisible(R.id.unauthenticated, !authViewModel.authenticated)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.signout -> {
                            alertDialog?.show()
                            true
                        }
                        R.id.signin -> {
                            findNavController(R.id.nav_host_fragment).navigate(R.id.action_feedFragment_to_signInFragment)
                            true
                        }
                        R.id.signup -> {
                            findNavController(R.id.nav_host_fragment).navigate(R.id.action_feedFragment_to_signUpFragment)
                            true
                        }
                        else -> false
                    }
            }.apply {
                menuProvider = this
            })
        }
    }

    private fun checkGoogleApiAvailability() {
        with(googleApiAvailability) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
                .show()
        }

        firebaseMessaging.token.addOnSuccessListener {
            println(it)
        }
    }
}