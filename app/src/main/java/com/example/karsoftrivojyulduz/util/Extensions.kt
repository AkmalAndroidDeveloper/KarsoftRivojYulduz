package com.example.karsoftrivojyulduz.util

import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.toastMessage(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}