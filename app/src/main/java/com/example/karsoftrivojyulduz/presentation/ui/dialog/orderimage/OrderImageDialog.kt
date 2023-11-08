package com.example.karsoftrivojyulduz.presentation.ui.dialog.orderimage

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.databinding.DialogOrderImageBinding

class OrderImageDialog : DialogFragment(R.layout.dialog_order_image) {

    private var _binding: DialogOrderImageBinding? = null
    private var imageUri: Uri? = null
    private var imageUrl: String? = null

    private val binding get() = _binding!!

    companion object {
        const val TAG = "OrderImageDialog"
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView(view)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        Glide
            .with(requireContext())
            .load(imageUri ?: imageUrl)
            .into(binding.ivOrderImage)
    }

    private fun bindView(view: View) {
        _binding = DialogOrderImageBinding.bind(view)
    }

    private fun unBindView() {
        _binding = null
    }

    fun setImageUrl(url: String) {
        imageUrl = url
    }

    fun setImageUri(uri: Uri) {
        imageUri = uri
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBindView()
    }
}