package com.example.karsoftrivojyulduz.presentation.ui.submitorder

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.databinding.FragmentSubmitOrderBinding
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.Image
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderImagesRequestData
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitOrderImagesData
import com.example.karsoftrivojyulduz.presentation.ui.dialog.cameraorgallery.CameraOrGalleryChooserDialog
import com.example.karsoftrivojyulduz.presentation.ui.dialog.loading.LoadingDialog
import com.example.karsoftrivojyulduz.presentation.ui.dialog.submitorder.SubmitOrderDialog
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.adapter.HistoryImagesAdapter
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.adapter.SubmitOrderImagesAdapter
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.viewmodel.SubmitOrderImagesCachingViewModel
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.viewmodel.SubmitOrderViewModel
import com.example.karsoftrivojyulduz.util.constant.Constants
import com.example.karsoftrivojyulduz.util.extension.toastMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class SubmitOrderFragment : Fragment(R.layout.fragment_submit_order) {

    private lateinit var cameraOrGalleryChooserDialog: CameraOrGalleryChooserDialog
    private lateinit var submitOrderDialog: SubmitOrderDialog
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var submitOrderImagesAdapter: SubmitOrderImagesAdapter
    private lateinit var historyImagesAdapter: HistoryImagesAdapter
    private lateinit var cameraRequestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var galleryRequestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var listOfDataFromCameraAndGallery: MutableList<SubmitImagesData>
    private lateinit var listOfSelectedImageForDelete: MutableMap<Int, SubmitImagesData>

    private var _binding: FragmentSubmitOrderBinding? = null
    private var customerPhoneNumber: String? = null
    private var orderAddress: String? = null
    private var customerName: String? = null
    private var orderTitle: String? = null
    private var currnetFetchedImageFilePathFromCamera: String? = null
    private var currentFetchedImageUriFromCamera: Uri? = null
    private var currentBitmap: Bitmap? = null
    private var hasImagesSelectable: Boolean = false
    private var fromCamera: Boolean = false
    private var fromHistoryFramgnet = false
    private var orderId: Int = Constants.UNDEFINED_ID
    private var imageOrderId: Int = 0

    private val submitOrderViewModel by viewModel<SubmitOrderViewModel>()
    private val submitOrderImagesCachingViewModel by viewModel<SubmitOrderImagesCachingViewModel>()
    private val arguments: SubmitOrderFragmentArgs by navArgs()
    private val binding get() = _binding!!

    companion object {
        private const val TAG = "SubmitOrderFragment"
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView(view)

        initRegistersForActivityResult()
        initValues()
        updateUI(fromHistoryFramgnet)
        getData()
        initObservables(fromHistoryFramgnet)
        initListeners()
    }

    private fun initDialogs() {
        loadingDialog = LoadingDialog()
        submitOrderDialog = SubmitOrderDialog()
        cameraOrGalleryChooserDialog = CameraOrGalleryChooserDialog()
    }

    private fun initLists() {
        listOfDataFromCameraAndGallery = mutableListOf()
        listOfSelectedImageForDelete = mutableMapOf()
    }

    private fun updateUI(fromHistoryFramgnet: Boolean) {
        with(binding) {
            if (fromHistoryFramgnet) {
                tvToolbar.text = getString(R.string.history_order)
                ivAddImage.visibility = View.INVISIBLE
                noPhotosContainer.visibility = View.INVISIBLE
                btnSumbitOrDeleteOrder.visibility = View.GONE
            } else {
                tvToolbar.text = getString(R.string.submit_order)
                ivAddImage.visibility = View.VISIBLE
                btnSumbitOrDeleteOrder.visibility = View.VISIBLE

                if (listOfDataFromCameraAndGallery.isEmpty()) {
                    recyclerViewImages.visibility = View.INVISIBLE
                    noPhotosContainer.visibility = View.VISIBLE
                } else {
                    recyclerViewImages.visibility = View.VISIBLE
                    noPhotosContainer.visibility = View.INVISIBLE
                }
            }
            tvToolbarNumberCounter.text = "№$orderId"
            tvTitle.text = orderTitle
            tvLocation.text = orderAddress
            tvCustomerName.text = customerName
            tvCustomerPhoneNumber.paintFlags =
                tvCustomerPhoneNumber.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            tvCustomerPhoneNumber.text = customerPhoneNumber
        }
    }

    private fun getData() {
        showLoadingDialog()
        when (fromHistoryFramgnet) {
            false -> lifecycleScope.launch {
                submitOrderImagesCachingViewModel.getAllImages(orderId)
            }

            true -> lifecycleScope.launch {
                submitOrderViewModel.getSimpleOrder(orderId)
            }
        }
    }

    private fun setUpRecyclerViewLayoutManager() {
        val layoutManager = GridLayoutManager(
            requireContext(), 2, GridLayoutManager.VERTICAL, false
        )
        binding.recyclerViewImages.layoutManager = layoutManager
    }

    private fun initValues() {
        with(arguments) {
            this@SubmitOrderFragment.orderId = orderId
            this@SubmitOrderFragment.fromHistoryFramgnet = fromHistoryFragment
            orderTitle = title
            orderAddress = address
            this@SubmitOrderFragment.customerName = customerName
            this@SubmitOrderFragment.customerPhoneNumber = customerPhoneNumber
        }

        initLists()
        initAdapters()
        setUpRecyclerViewLayoutManager()
        setUpRecyclerViewAdapter(fromHistoryFramgnet)
        initDialogs()
    }

    private fun initRegistersForActivityResult() {
        cameraRequestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) openCamera()
            else requestCameraPermission()
        }
        cameraResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                fromCamera = true
                fetchImageFromCamera()
                changeVisibilityIfListOfImagesNotEmpty()
            }
        }
        galleryRequestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) openGallery()
            else requestGalleryPermission()
        }
        galleryResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                fromCamera = false
                fetctSelectedImagesFromGallery(it)
                changeVisibilityIfListOfImagesNotEmpty()
            }
        }
    }

    private fun fetchImageFromCamera() {
        if (hasListOfImagesFromCameraAndGalleryLengthMoreThanTen()) {
            toastMessage(getString(R.string.the_number_of_photos_should_not_exceed_ten))
        } else {
            incrementImageOrderId()

            val imageData = SubmitImagesData(
                imageOrderId, orderId, currentFetchedImageUriFromCamera.toString()
            )

            listOfDataFromCameraAndGallery.add(imageData)
            addImageToSubmitOrderImagesAdapterList(imageData)
            lifecycleScope.launch {
                submitOrderImagesCachingViewModel.insertImage(imageData)
            }
        }
    }

    private fun fetctSelectedImagesFromGallery(it: ActivityResult?) {
        if (it?.data?.clipData != null) {
            val countImages = it.data?.clipData?.itemCount
            if ((countImages?.plus(listOfDataFromCameraAndGallery.size))!! <= 10) for (i in 0 until countImages) {
                if (hasListOfImagesFromCameraAndGalleryLengthMoreThanTen()) toastMessage(getString(R.string.the_number_of_photos_should_not_exceed_ten))
                else {
                    incrementImageOrderId()

                    val imageUri = it.data?.clipData?.getItemAt(i)?.uri
                    val imageData = SubmitImagesData(
                        imageOrderId, orderId, imageUri.toString()
                    )

                    listOfDataFromCameraAndGallery.add(imageData)
                    addImageToSubmitOrderImagesAdapterList(imageData)
                    lifecycleScope.launch {
                        submitOrderImagesCachingViewModel.insertImage(imageData)
                    }
                }
            }
            else toastMessage(getString(R.string.the_number_of_photos_should_not_exceed_ten))
        } else if (it?.data?.data != null) {
            if (hasListOfImagesFromCameraAndGalleryLengthMoreThanTen()) {
                toastMessage(getString(R.string.the_number_of_photos_should_not_exceed_ten))
            } else {
                incrementImageOrderId()

                val imageUri = it.data?.data
                val imageData = SubmitImagesData(
                    imageOrderId, orderId, imageUri.toString()
                )

                listOfDataFromCameraAndGallery.add(imageData)
                addImageToSubmitOrderImagesAdapterList(imageData)
                lifecycleScope.launch {
                    submitOrderImagesCachingViewModel.insertImage(imageData)
                }
            }
        }
    }

    private fun changeVisibilityIfListOfImagesNotEmpty() {
        if (listOfDataFromCameraAndGallery.isNotEmpty()) {
            binding.recyclerViewImages.visibility = View.VISIBLE
            binding.noPhotosContainer.visibility = View.INVISIBLE
        }
    }

    private fun changeVisibilityIfListEmpty(list: List<*>) {
        with(binding) {
            if (list.isNotEmpty()) {
                noPhotosContainer.visibility = View.INVISIBLE
                recyclerViewImages.visibility = View.VISIBLE
            } else {
                noPhotosContainer.visibility = View.VISIBLE
                recyclerViewImages.visibility = View.INVISIBLE
            }
        }
    }

    private fun hasListOfImagesFromCameraAndGalleryLengthMoreThanTen() =
        listOfDataFromCameraAndGallery.size > 9

    private fun requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasReadMediaImagesPermission()) {
                requestReadMediaImagesPermission()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!hasManageExternalStoragePermission()) {
                requestManageExternalStoragePermission()
            }
        } else {
            if (!hasReadExternalStoragePermission()) {
                requestReadExternalStoragePermission()
            }
        }
    }

    private fun bindView(view: View) {
        _binding = FragmentSubmitOrderBinding.bind(view)
    }

    private fun unBindView() {
        _binding = null
    }

    private fun initAdapters() {
        submitOrderImagesAdapter = SubmitOrderImagesAdapter(requireContext())
        historyImagesAdapter = HistoryImagesAdapter(requireContext())
    }

    private fun setUpRecyclerViewAdapter(fromHistoryFragment: Boolean) {
        if (fromHistoryFragment) binding.recyclerViewImages.adapter = historyImagesAdapter
        else binding.recyclerViewImages.adapter = submitOrderImagesAdapter
    }

    private fun initHistoryImagesAdapterList(listOfImages: List<Image>) {
        historyImagesAdapter.sumbitList(listOfImages)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun initObservables(fromHistoryFramgnet: Boolean) {
        listOfDataFromCameraAndGallery.clear()
        listOfSelectedImageForDelete.clear()
        when (fromHistoryFramgnet) {
            true -> with(submitOrderViewModel) {
                successSimpleOrderFlow.onEach {
                    Log.d(TAG, "Success: ${it.data.images}")
                    if (fromHistoryFramgnet) initHistoryImagesAdapterList(it.data.images)
                    if (loadingDialog.isVisible) dismissLoadingDialog()
                }.launchIn(lifecycleScope)
                messageSimpleOrderFlow.onEach {
                    Log.d(TAG, "Message: $it")
                }.launchIn(lifecycleScope)
                errorSimpleOrderFlow.onEach {
                    Log.d(TAG, "Error: $it")
                }.launchIn(lifecycleScope)
            }

            false -> {
                with(submitOrderViewModel) {
                    successOrderImagesFlow.onEach {
                        Log.d(TAG, "success: $it")
                        toastMessage("Завершено успешно")
                        dismissLoadingDialog()
                        popBackStack()
                    }.launchIn(viewModelScope)
                }
                with(submitOrderImagesCachingViewModel) {
                    successSubmitOrderImagesFlow.onEach {
                        if (it.isEmpty()) {
                            dismissLoadingDialog()
                            binding.recyclerViewImages.visibility = View.INVISIBLE
                            binding.noPhotosContainer.visibility = View.VISIBLE
                        } else {
                            if (loadingDialog.isVisible) dismissLoadingDialog()
                            binding.recyclerViewImages.visibility = View.VISIBLE
                            binding.noPhotosContainer.visibility = View.INVISIBLE
                        }
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (it.isNotEmpty()) {
                                imageOrderId = it.size
                                listOfDataFromCameraAndGallery.clear()
                                submitOrderImagesAdapter.clearList()

                                listOfDataFromCameraAndGallery.addAll(it)
                                withContext(Dispatchers.Main) {
                                    initSubmitOrderImagesAdapterList(listOfDataFromCameraAndGallery)
                                    if (loadingDialog.isVisible) dismissLoadingDialog()
                                }
                            }
                        }
                    }
                }.launchIn(lifecycleScope)
            }
        }
    }

    private fun dismissLoadingDialog() {
        loadingDialog.dismiss()
    }

    private fun dismissLoadingDialog(list: List<*>) {
        if (list.isEmpty()) loadingDialog.dismiss()
    }

    private fun addImageToSubmitOrderImagesAdapterList(imageData: SubmitImagesData) {
        submitOrderImagesAdapter.addImage(imageData)
    }

    private fun initSubmitOrderImagesAdapterList(list: List<SubmitImagesData>) {
        submitOrderImagesAdapter.submitList(list)
    }

    private fun initListeners() {
        with(binding) {
            ivBack.setOnClickListener {
                if (hasImagesSelectable) {
                    if (listOfSelectedImageForDelete.isEmpty()) {
                        hasImagesSelectable = false
                        ivAddImage.visibility = View.VISIBLE
                        tvToolbarNumberCounter.visibility = View.VISIBLE
                        tvToolbar.text = getString(R.string.submit_order)
                        btnSumbitOrDeleteOrder.text = getString(R.string.submit_order_btn)
                    }
                } else {
                    popBackStack()
                }
            }
            ivAddImage.setOnClickListener {
                if (!cameraOrGalleryChooserDialog.isVisible) showCameraOrGalleryChooserDialog()
            }
            btnSumbitOrDeleteOrder.setOnClickListener {
                if (hasImagesSelectable) {
                    if (listOfSelectedImageForDelete.isNotEmpty()) {
                        for (data in listOfSelectedImageForDelete.values) {
                            listOfDataFromCameraAndGallery.remove(data)
                            lifecycleScope.launch(Dispatchers.IO) {
                                val imageData = SubmitImagesData(
                                    data.id, data.orderId, data.uri
                                )
                                submitOrderImagesCachingViewModel.deleteImage(imageData)
                            }
                        }
                        submitOrderImagesAdapter.deleteImage(listOfSelectedImageForDelete.values.toList())
                        listOfSelectedImageForDelete.clear()

                        hasImagesSelectable = false
                        ivAddImage.visibility = View.VISIBLE
                        tvToolbarNumberCounter.visibility = View.VISIBLE
                        tvToolbar.text = getString(R.string.submit_order)
                        btnSumbitOrDeleteOrder.text = getString(R.string.submit_order_btn)
                        submitOrderImagesAdapter.notifyDataSetChanged()
                        changeVisibilityIfListEmpty(listOfDataFromCameraAndGallery)
                    } else {
                        toastMessage(getString(R.string.choose_photo_for_delete))
                    }
                } else if (!submitOrderDialog.isVisible) showSubmitOrderDialog()
            }
            cameraOrGalleryChooserDialog.onCameraClickListener {
                checkAndOpenCamera()
            }
            cameraOrGalleryChooserDialog.onGalleryClickListener {
                checkAndOpenGallery()
            }
            submitOrderDialog.onYesButtonClickListener {
                if (listOfDataFromCameraAndGallery.isEmpty()) toastMessage("Добавьте фотографии")
                else {
                    showLoadingDialog()
                    lifecycleScope.launch(Dispatchers.IO) {
                        val orderId = RequestBody.create(MultipartBody.FORM, orderId.toString())
                        val description = RequestBody.create(
                            MultipartBody.FORM, "Фотографии заказа №$orderId"
                        )
                        val images = listOfDataFromCameraAndGallery.map { data ->
                            (convertUriToMultipartBodyPart(
                                data.uri.toUri(), requireContext().contentResolver
                            ))
                        }
                        submitOrderViewModel.insertOrderImages(
                            OrderImagesRequestData(
                                orderId, description, images
                            )
                        )
                    }
                }
            }
            submitOrderImagesAdapter.setOnItemClickListener { image, imagePosition, cardSelect ->
                if (hasImagesSelectable) {
                    if (cardSelect.isVisible) {
                        cardSelect.visibility = View.INVISIBLE
                        listOfSelectedImageForDelete.remove(imagePosition)

                        if (listOfSelectedImageForDelete.isEmpty()) {
                            hasImagesSelectable = false
                            ivAddImage.visibility = View.VISIBLE
                            tvToolbarNumberCounter.visibility = View.VISIBLE
                            tvToolbar.text = getString(R.string.submit_order)
                            btnSumbitOrDeleteOrder.text = getString(R.string.submit_order_btn)
                        }
                    } else {
                        cardSelect.visibility = View.VISIBLE
                        listOfSelectedImageForDelete[imagePosition] = image
                    }
                } else {

                    val direction =
                        SubmitOrderFragmentDirections.actionOrderFragmentToOrderImageFragment(
                            image.uri, imagePosition, fromHistoryFramgnet
                        )
                    navigateTo(direction)
                }
            }
            submitOrderImagesAdapter.setOnLongClickListener { image, hasClick, imagePosition, cardSelect ->
                if (!hasImagesSelectable) {
                    hasImagesSelectable = hasClick
                    ivAddImage.visibility = View.INVISIBLE
                    tvToolbarNumberCounter.visibility = View.INVISIBLE
                    tvToolbar.text = getString(R.string.viberite_neskolko)
                    btnSumbitOrDeleteOrder.text = getString(R.string.delete)
                    if (!cardSelect.isVisible) {
                        cardSelect.visibility = View.VISIBLE
                        listOfSelectedImageForDelete[imagePosition] = image
                    }
                }
            }
            historyImagesAdapter.setOnItemClickListener { image, imagePosition, _ ->
                val direction =
                    SubmitOrderFragmentDirections.actionOrderFragmentToOrderImageFragment(
                        image.image_url, imagePosition, fromHistoryFramgnet
                    )
                navigateTo(direction)
            }
            tvCustomerPhoneNumber.setOnClickListener {
                call(tvCustomerPhoneNumber.text.toString())
            }
            loadingDialog.setOnCancelClickListener {
                dismissLoadingDialog()
                popBackStack()
            }
        }
    }

    private fun showLoadingDialog() {
        loadingDialog.show(childFragmentManager, null)
    }

    private fun checkAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasReadMediaImagesPermission()) {
                openGallery()
            } else {
                requestReadMediaImagesPermission()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (hasManageExternalStoragePermission()) {
                openGallery()
            } else {
                requestManageExternalStoragePermission()
            }
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (hasReadExternalStoragePermission()) {
                openGallery()
            } else {
                requestReadExternalStoragePermission()
            }
        }
    }

    private fun checkAndOpenCamera() {
        if (hasCameraPermission()) {
            openCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun incrementImageOrderId() {
        ++imageOrderId
    }

    private fun popBackStack() {
        findNavController().popBackStack()
    }

    private fun convertUriToMultipartBodyPart(
        uri: Uri?, contentResolver: ContentResolver
    ): MultipartBody.Part {
        val bitmap = Images.Media.getBitmap(contentResolver, uri)
        val byteArrayOutpuStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutpuStream)
        val byteArray = byteArrayOutpuStream.toByteArray()
        val image = RequestBody.create("image/*".toMediaTypeOrNull(), byteArray)
        return MultipartBody.Part.createFormData("images[]", File(uri?.path ?: "").name, image)
    }

    private fun navigateTo(direction: NavDirections) {
        findNavController().navigate(direction)
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasReadExternalStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasReadMediaImagesPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hasManageExternalStoragePermission(): Boolean {
        return Environment.isExternalStorageManager()
    }

    private fun requestCameraPermission() {
        cameraRequestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun requestReadExternalStoragePermission() {
        galleryRequestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun requestReadMediaImagesPermission() {
        galleryRequestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
    }

    private fun requestManageExternalStoragePermission() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.addCategory("android.intent.category.DEFAULT")
            intent.data = Uri.parse(String.format("package:%s", requireContext().packageName))
            galleryResultLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            galleryResultLauncher.launch(intent)
        }
    }

    private fun openGallery() {
        val intent = Intent(
            Intent.ACTION_GET_CONTENT, Images.Media.EXTERNAL_CONTENT_URI
        ) //ACTION_GET_DOCUMENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        galleryResultLauncher.launch(intent)
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            val photoFile: File? = createImageFile()
            if (photoFile != null) {
                currentFetchedImageUriFromCamera = FileProvider.getUriForFile(
                    requireContext(), Constants.AUTHORITY, photoFile
                )
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentFetchedImageUriFromCamera)
                cameraResultLauncher.launch(cameraIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "Camera exception: $e")
        }
    }

    private fun showCameraOrGalleryChooserDialog() {
        cameraOrGalleryChooserDialog.show(childFragmentManager, null)
    }

    private fun showSubmitOrderDialog() {
        submitOrderDialog.show(childFragmentManager, null)
    }

    private fun call(phoneNumber: String) {
        Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
            startActivity(this)
        }
    }

    private fun saveImage(bitmap: Bitmap, title: String) {
        @Suppress("DEPRECATION") Images.Media.insertImage(
            requireContext().contentResolver, bitmap, title, null
        )
    }

    private fun getImageUri(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 10, bytes)
        val date = SimpleDateFormat("yyyy_MM_dd", Locale.getDefault()).format(Date())
        val path = Images.Media.insertImage(
            context.contentResolver, bitmap, "ORDER_IMG_${date}_${System.currentTimeMillis()}", null
        )
        return Uri.parse(path)
    }

    private fun getImageUri(inImage: Bitmap): Uri {
        val tempFile = File.createTempFile("temprentpk", ".png")
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val bitmapData = bytes.toByteArray()

        val fileOutPut = FileOutputStream(tempFile)
        fileOutPut.write(bitmapData)
        fileOutPut.flush()
        fileOutPut.close()
        return Uri.fromFile(tempFile)
    }

    private fun createImageFile(): File? {
        val date = SimpleDateFormat("yyyy_MM_dd", Locale.getDefault()).format(Date())
        val imageFileName = "ORDER_IMG_${date}_${System.currentTimeMillis()}"
        val storageDirectory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(
            imageFileName, Constants.JPG, storageDirectory
        )
        currnetFetchedImageFilePathFromCamera = imageFile.absolutePath
        return imageFile
    }

    private suspend fun getBitmapFromUri(uri: Uri): Bitmap {
        return Images.Media.getBitmap(
            requireActivity().contentResolver, uri
        )
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private suspend fun getBitmap(imageUri: Uri): Bitmap {
        val source = ImageDecoder.createSource(requireContext().contentResolver, imageUri)
        return ImageDecoder.decodeBitmap(source)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBindView()
        listOfDataFromCameraAndGallery.clear()
        listOfSelectedImageForDelete.clear()
    }
}