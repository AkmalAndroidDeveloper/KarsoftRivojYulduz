package com.example.karsoftrivojyulduz.presentation.ui.dialog.permission

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.databinding.DialogLogOutBinding
import com.example.karsoftrivojyulduz.databinding.DialogRequestPermissionBinding
import com.example.karsoftrivojyulduz.util.constant.Permission

class RequestPermissionDialog : DialogFragment(R.layout.dialog_request_permission) {

    private var _binding: DialogRequestPermissionBinding? = null
    private var onYesButtonClick: ((View) -> Unit)? = null
    private var onNoButtonClick: ((View) -> Unit)? = null

    private val binding get() = _binding!!

    companion object {
        const val TAG = "RequestPermissionDialog"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView(view)

        setDialogBackgroundTransparent()
        initListeners()
    }

    private fun initListeners() {
        with(binding) {
            btnYes.setOnClickListener {
                onYesButtonClick?.invoke(it)
                dismiss()
            }
            btnNo.setOnClickListener {
                onNoButtonClick?.invoke(it)
                dismiss()
            }
        }
    }

    private fun setDialogBackgroundTransparent() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun unBindView() {
        _binding = null
    }

    private fun bindView(view: View) {
        _binding = DialogRequestPermissionBinding.bind(view)
    }

    fun onYesButtonClickListener(block: (View) -> Unit) {
        onYesButtonClick = block
    }

    fun onNoButtonClickListener(block: (View) -> Unit) {
        onNoButtonClick = block
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBindView()
    }
}