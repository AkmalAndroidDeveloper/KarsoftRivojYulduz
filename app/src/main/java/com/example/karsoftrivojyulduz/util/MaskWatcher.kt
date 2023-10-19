package com.example.karsoftrivojyulduz.util

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class MaskWatcher(
    private val mask: String
) : TextWatcher {
    private var isRunning = false
    private var isDeleting = false

    override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
        isDeleting = count > after
    }
    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(editable: Editable) {
        if (isRunning || isDeleting) {
            return
        }
        isRunning = true
        val editableLength = editable.length
        val editableText = editable.toString()

        if (editableLength < mask.length) {
            if (mask[editableLength] != '#') {
                editable.append(mask[editableLength])
            } else if (mask[editableLength - 1] != '#') {
                editable.insert(editableLength - 1, mask, editableLength - 1, editableLength)
            }
        }

        isRunning = false
    }
}