package ru.netology.nmedia.activity

import androidx.fragment.app.DialogFragment

interface DialogListener {
        fun onDialogSignInClick(dialog: DialogFragment)
        fun onDialogSignUpClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }
