package com.example.karsoftrivojyulduz.presentation.ui.dialog.submitorder

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.databinding.DialogSubmitOrderBinding

class SubmitOrderDialog : DialogFragment(R.layout.dialog_submit_order) {

    private var _binding: DialogSubmitOrderBinding? = null
    private var onYesButtonClick: ((View) -> Unit)? = null
    private var onNoButtonClick: ((View) -> Unit)? = null

    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView(view)

        setDialogBackgroundTransparent()
        initListeners()
    }

    private fun bindView(view: View) {
        _binding = DialogSubmitOrderBinding.bind(view)
    }

    private fun unBindView() {
        _binding = null
    }

    private fun setDialogBackgroundTransparent() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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