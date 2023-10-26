package com.example.karsoftrivojyulduz.presentation.ui.dialog.loading

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.databinding.DialogLoadingBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoadingDialog: DialogFragment(R.layout.dialog_loading) {

    private var _binding: DialogLoadingBinding? = null

    private val binding get() = _binding!!
    private var onClickCancel: (() -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView(view)

        setDialogCancelable()
        setDialogBackgroundTransparent()

        binding.ivCancel.setOnClickListener {
            onClickCancel?.invoke()
        }

        lifecycleScope.launch {
            delay(5000)
            binding.ivCancel.visibility = View.VISIBLE
        }
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

    fun setOnCancelClickListener(block: () -> Unit) {
        onClickCancel = block
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBindView()
    }
}