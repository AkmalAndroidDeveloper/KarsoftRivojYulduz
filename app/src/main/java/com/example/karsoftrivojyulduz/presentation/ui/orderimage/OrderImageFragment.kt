package com.example.karsoftrivojyulduz.presentation.ui.orderimage

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.databinding.FragmentOrderImageBinding
import com.example.karsoftrivojyulduz.util.extension.toastMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class OrderImageFragment : Fragment(R.layout.fragment_order_image) {

    private var _binding: FragmentOrderImageBinding? = null
    private var image: String? = null
    private var position: Int? = null
    private var fromHistoryFragment: Boolean = false

    private val binding get() = _binding!!
    private val arguments: OrderImageFragmentArgs by navArgs()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView(view)

        initValues()
        initListeners()

        with(binding) {
            if (!fromHistoryFragment) {
                Glide
                    .with(requireContext())
                    .load(image?.toUri())
                    .centerCrop()
                    .into(ivOrderImage)
            } else {
                Glide
                    .with(requireContext())
                    .load(image)
                    .centerCrop()
                    .into(ivOrderImage)
            }
            tvPhotoNumber.text = "№${position?.plus(1)}"
            Glide
                .with(requireContext())
                .load(image)
                .centerCrop()
                .into(ivOrderImage)
        }
    }

    private fun initListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initValues() {
        image = arguments.image
        position = arguments.position
        fromHistoryFragment = arguments.fromHistoryFragment
    }

    private fun bindView(view: View) {
        _binding = FragmentOrderImageBinding.bind(view)
    }

    private fun unBindView() {
        _binding = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBindView()
    }
}