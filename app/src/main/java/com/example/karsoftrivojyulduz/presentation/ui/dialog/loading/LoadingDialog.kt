package com.example.karsoftrivojyulduz.presentation.ui.dialog.loading

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.databinding.DialogLoadingBinding

class LoadingDialog: DialogFragment(R.layout.dialog_loading) {

    private var _binding: DialogLoadingBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView(view)

        setDialogCancelable()
        setDialogBackgroundTransparent()
    }

    private fun setDialogBackgroundTransparent() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setDialogCancelable() {
        dialog?.setCancelable(false)
    }

    private fun unBindView() {
        _binding = null
    }

    private fun bindView(view: View) {
        _binding = DialogLoadingBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBindView()
    }
}