package com.example.karsoftrivojyulduz.presentation.ui.dialog.cameraorgallery

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.databinding.DialogCameraOrGalleryBinding

class CameraOrGalleryChooserDialog : DialogFragment(R.layout.dialog_camera_or_gallery) {

    private var _binding: DialogCameraOrGalleryBinding? = null
    private var onCameraClick: ((View) -> Unit)? = null
    private var onGalleryClick: ((View) -> Unit)? = null

    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView(view)

        setDialogBackgroundTransparent()
        initListeners()
    }

    private fun bindView(view: View) {
        _binding = DialogCameraOrGalleryBinding.bind(view)
    }

    private fun unBindView() {
        _binding = null
    }

    private fun setDialogBackgroundTransparent() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun initListeners() {
        with(binding) {
            ivCamera.setOnClickListener {
                onCameraClick?.invoke(it)
                dismiss()
            }
            ivGallery.setOnClickListener {
                onGalleryClick?.invoke(it)
                dismiss()
            }
        }
    }

    fun onCameraClickListener(block: (View) -> Unit) {
        onCameraClick = block
    }

    fun onGalleryClickListener(block: (View) -> Unit) {
        onGalleryClick = block
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBindView()
    }
}