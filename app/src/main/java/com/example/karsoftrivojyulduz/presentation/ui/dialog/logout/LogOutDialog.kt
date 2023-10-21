package com.example.karsoftrivojyulduz.presentation.ui.dialog.logout

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.databinding.DialogLogOutBinding

class LogOutDialog : DialogFragment(R.layout.dialog_log_out) {

    private var _binding: DialogLogOutBinding? = null
    private var onYesButtonClick: ((View) -> Unit)? = null
    private var onNoButtonClick: ((View) -> Unit)? = null

    private val binding get() = _binding!!

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
        _binding = DialogLogOutBinding.bind(view)
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