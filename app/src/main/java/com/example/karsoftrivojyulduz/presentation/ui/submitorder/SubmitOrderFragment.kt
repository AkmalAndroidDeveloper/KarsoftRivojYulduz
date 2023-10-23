package com.example.karsoftrivojyulduz.presentation.ui.submitorder

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesCacheData
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData
import com.example.karsoftrivojyulduz.presentation.ui.dialog.cameraorgallery.CameraOrGalleryChooserDialog
import com.example.karsoftrivojyulduz.presentation.ui.dialog.loading.LoadingDialog
import com.example.karsoftrivojyulduz.presentation.ui.dialog.submitorder.SubmitOrderDialog
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.adapter.HistoryImagesAdapter
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.adapter.SubmitOrderImagesAdapter
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.viewmodel.SubmitOrderImagesCachingViewModel
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.viewmodel.SubmitOrderViewModel
import com.example.karsoftrivojyulduz.util.constant.Constants
import com.example.karsoftrivojyulduz.util.extension.toastMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class SubmitOrderFragment : Fragment(R.layout.fragment_submit_order) {

    private lateinit var submitOrderImagesAdapter: SubmitOrderImagesAdapter
    private lateinit var historyImagesAdapter: HistoryImagesAdapter
    private lateinit var cameraRequestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryRequestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var galleryResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraOrGalleryChooserDialog: CameraOrGalleryChooserDialog
    private lateinit var submitOrderDialog: SubmitOrderDialog
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var listOfDataFromCameraAndGallery: MutableList<SubmitImagesData>
    private lateinit var listOfSelectedImageForDelete: MutableMap<Int, String>

    private var _binding: FragmentSubmitOrderBinding? = null
    private var currentImagePathFromCamera: String? = null
    private var orderTitle: String? = null
    private var orderAddress: String? = null
    private var customerName: String? = null
    private var customerPhoneNumber: String? = null
    private var hasImagesSelectable: Boolean = false
    private var fromCamera: Boolean = false
    private var fromHistoryFramgnet = false
    private var orderId: Int = Constants.UNDEFINED_ID
    private var imageOrderId: Int = 0

    private val binding get() = _binding!!
    private val arguments: SubmitOrderFragmentArgs by navArgs()
    private val submitOrderViewModel by viewModel<SubmitOrderViewModel>()
    private val submitOrderImagesCachingViewModel by viewModel<SubmitOrderImagesCachingViewModel>()
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    companion object {
        private const val TAG = "SubmitOrderFragment"
    }

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
            tvCustomerPhoneNumber.text = customerPhoneNumber
        }
    }

    private fun getData() {
        showLoadingDialog()
        lifecycleScope.launch {
            if (!fromHistoryFramgnet)
                submitOrderImagesCachingViewModel.getAllImages(orderId)
            else
                submitOrderViewModel.getSimpleOrder(orderId)
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
            if (isGranted) openCamera("order_${orderId}_${imageOrderId}")
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
        uiScope.launch(Dispatchers.IO) {
            if (hasListOfImagesFromCameraAndGalleryLengthMoreThanTen()) {
                withContext(Dispatchers.Main) {
                    toastMessage(getString(R.string.the_number_of_photos_should_not_exceed_ten))
                }
            } else {
                incrementImageOrderId()

                val bitmap = BitmapFactory.decodeFile(currentImagePathFromCamera)
                val imageData = SubmitImagesData(
                    imageOrderId,
                    orderId,
                    getImageUri(requireContext(), bitmap)!!,
                    bitmap
                )
                val submitImagesCacheData = SubmitImagesCacheData(
                    orderId = orderId,
                    uri = imageData.uri.toString()
                )

                listOfDataFromCameraAndGallery.add(imageData)

                withContext(Dispatchers.Main) {
                    addImageToSubmitOrderImagesAdapterList(imageData)
                }
            }
        }
    }

    private fun fetctSelectedImagesFromGallery(it: ActivityResult?) {
        uiScope.launch(Dispatchers.IO) {
            if (it?.data?.clipData != null) {
                val countImages = it.data?.clipData?.itemCount
                if ((countImages?.plus(listOfDataFromCameraAndGallery.size))!! <= 10)
                    for (i in 0 until countImages) {
                        if (hasListOfImagesFromCameraAndGalleryLengthMoreThanTen())
                            withContext(Dispatchers.Main) {
                                toastMessage(getString(R.string.the_number_of_photos_should_not_exceed_ten))
                            }
                        else {
                            incrementImageOrderId()

                            val imageUri = it.data?.clipData?.getItemAt(i)?.uri
                            val bitmap = Images.Media.getBitmap(
                                requireContext().contentResolver, imageUri
                            )
                            val imageData = SubmitImagesData(
                                imageOrderId,
                                orderId,
                                getImageUri(requireContext(), bitmap) ?: Uri.parse(""),
                                bitmap
                            )
                            val submitImagesCacheData = SubmitImagesCacheData(
                                orderId = orderId,
                                uri = imageData.uri.toString()
                            )

                            listOfDataFromCameraAndGallery.add(imageData)
                            withContext(Dispatchers.Main) {
                                addImageToSubmitOrderImagesAdapterList(imageData)
                            }
                        }
                    }
                else
                    withContext(Dispatchers.Main) {
                        toastMessage(getString(R.string.the_number_of_photos_should_not_exceed_ten))
                    }
            } else if (it?.data?.data != null) {
                if (hasListOfImagesFromCameraAndGalleryLengthMoreThanTen()) {
                    withContext(Dispatchers.Main) {
                        toastMessage(getString(R.string.the_number_of_photos_should_not_exceed_ten))
                    }
                } else {
                    incrementImageOrderId()

                    val imageUri = it.data?.data
                    val bitmap = Images.Media.getBitmap(
                        requireContext().contentResolver, imageUri
                    )
                    val imageData = SubmitImagesData(
                        imageOrderId,
                        orderId,
                        getImageUri(requireContext(), bitmap) ?: Uri.parse(""),
                        bitmap
                    )

                    listOfDataFromCameraAndGallery.add(imageData)
                    withContext(Dispatchers.Main) {
                        addImageToSubmitOrderImagesAdapterList(imageData)
                    }
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

    private fun changeVisibilityIfListEmpty(list: List<SubmitImagesCacheData>) {
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
        listOfDataFromCameraAndGallery.size > 10

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
//        else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
//            if(!hasReadExternalStoragePermission()) {
//                requestReadExternalStoragePermission()
//            }
//        }
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

    private fun initObservables(fromHistoryFramgnet: Boolean) {
        if (!fromHistoryFramgnet) {
            with(submitOrderViewModel) {
                successOrderImagesFlow.onEach {
                    toastMessage("Завершено успешно")
                    dismissLoadingDialog()
                    popBackStack()
                }.launchIn(viewModelScope)
            }
            with(submitOrderImagesCachingViewModel) {
                successSubmitOrderImagesFlow.onEach {
                    changeVisibilityIfListEmpty(it)
                    Log.d(TAG, "Size: ${it.size}")

                    uiScope.launch(Dispatchers.IO) {
                        imageOrderId = it.size

                        for (data in it) {
                            val image = Images.Media.getBitmap(
                                requireContext().contentResolver,
                                data.uri.toUri()
                            )
                            val submitImagesData = SubmitImagesData(
                                data.id!!,
                                data.orderId,
                                data.uri.toUri(),
                                image
                            )
                            listOfDataFromCameraAndGallery.add(submitImagesData)
                            Log.d(TAG, "initObservables: $listOfDataFromCameraAndGallery")
                            withContext(Dispatchers.Main) {
                                addImageToSubmitOrderImagesAdapterList(submitImagesData)
                            }
                        }
                    }
                    dismissLoadingDialog()
                }.launchIn(lifecycleScope)
            }
        } else
            with(submitOrderViewModel) {
                successSimpleOrderFlow.onEach {
                    Log.d(TAG, "Success: ${it.data.images}")
                    if (fromHistoryFramgnet)
                        initHistoryImagesAdapterList(it.data.images)
                    dismissLoadingDialog()
                }.launchIn(lifecycleScope)
                messageSimpleOrderFlow.onEach {
                    Log.d(TAG, "Message: $it")
                }.launchIn(lifecycleScope)
                errorSimpleOrderFlow.onEach {
                    Log.d(TAG, "Error: $it")
                }.launchIn(lifecycleScope)
            }
    }

    private fun dismissLoadingDialog() {
        loadingDialog.dismiss()
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
                if (!cameraOrGalleryChooserDialog.isVisible)
                    showCameraOrGalleryChooserDialog()
            }
            btnSumbitOrDeleteOrder.setOnClickListener {
                if (hasImagesSelectable) {
                    if (listOfSelectedImageForDelete.isNotEmpty()) {
                        for (i in listOfSelectedImageForDelete.keys) {
                            listOfDataFromCameraAndGallery.removeAt(i)
                        }
                        hasImagesSelectable = false
                        ivAddImage.visibility = View.VISIBLE
                        tvToolbarNumberCounter.visibility = View.VISIBLE
                        tvToolbar.text = getString(R.string.submit_order)
                        btnSumbitOrDeleteOrder.text = getString(R.string.submit_order_btn)
                    } else {
                        toastMessage(getString(R.string.choose_photo_for_delete))
                    }
                } else
                    if (!submitOrderDialog.isVisible)
                        showSubmitOrderDialog()
            }
            cameraOrGalleryChooserDialog.onCameraClickListener {
                checkAndOpenCamera()
            }
            cameraOrGalleryChooserDialog.onGalleryClickListener {
                checkAndOpenGallery()
            }
            submitOrderDialog.onYesButtonClickListener {
                uiScope.launch(Dispatchers.IO) {
                    if (listOfDataFromCameraAndGallery.isEmpty())
                        withContext(Dispatchers.Main) {
                            toastMessage("Добавьте фотографии")
                        }
                    else {
                        showLoadingDialog()
                        val orderId = RequestBody.create(MultipartBody.FORM, orderId.toString())
                        val description =
                            RequestBody.create(MultipartBody.FORM, "Фотографии заказа №$orderId")
                        val images = listOfDataFromCameraAndGallery.map { data ->
                            (convertUriToMultipartBodyPart(data.uri, requireActivity().contentResolver))
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
                        listOfSelectedImageForDelete[imagePosition] = image.toString()
                    }
                } else {
                    val direction =
                        SubmitOrderFragmentDirections.actionOrderFragmentToOrderImageFragment(
                            image.uri.toString(),
                            imagePosition,
                            fromHistoryFramgnet
                        )
                    navigateTo(direction)
                }
            }
            submitOrderImagesAdapter.setOnLongClickListener {
                hasImagesSelectable = it
                ivAddImage.visibility = View.INVISIBLE
                tvToolbarNumberCounter.visibility = View.INVISIBLE
                tvToolbar.text = getString(R.string.viberite_neskolko)
                btnSumbitOrDeleteOrder.text = getString(R.string.delete)
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
            openCamera("order_${orderId}_${imageOrderId}")
        } else {
            requestCameraPermission()
        }
    }

    private fun incrementImageOrderId() {
        ++imageOrderId
        Log.d(TAG, "imageOrderIdGallery: $imageOrderId")
    }

    private fun popBackStack() {
        findNavController().popBackStack()
    }

    private fun convertUriToMultipartBodyPart(
        uri: Uri?, contentResolver: ContentResolver
    ): MultipartBody.Part {
        val bitmap = Images.Media.getBitmap(contentResolver, uri)
        val byteArrayOutpuStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutpuStream)
        val byteArray = byteArrayOutpuStream.toByteArray()
        val image = RequestBody.create("image/*".toMediaTypeOrNull(), byteArray)
        return MultipartBody.Part.createFormData("images[]", File(uri?.path ?: "").name, image)
    }

    private fun getImageUri(context: Context, bitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes)
        val date = SimpleDateFormat("yyyy_MM_dd", Locale.getDefault()).format(Date())
        val path = Images.Media.insertImage(
            context.contentResolver,
            bitmap,
            "ORDER_IMG_${date}_${System.currentTimeMillis()}",
            null
        )
        return Uri.parse(path)
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
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        galleryResultLauncher.launch(intent)
    }

    private fun openCamera(fileName: String) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val storageDirectory =
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(fileName, Constants.JPG, storageDirectory)
        try {
            currentImagePathFromCamera = imageFile.absolutePath
            val imageUri = FileProvider.getUriForFile(
                requireContext(), Constants.AUTHORITY, imageFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            cameraResultLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showCameraOrGalleryChooserDialog() {
        cameraOrGalleryChooserDialog.show(childFragmentManager, null)
    }

    private fun showSubmitOrderDialog() {
        submitOrderDialog.show(childFragmentManager, null)
    }

    private fun saveImage(bitmap: Bitmap, title: String) {
        @Suppress("DEPRECATION") Images.Media.insertImage(
            requireContext().contentResolver, bitmap, title, null
        )
    }

    private fun call(phoneNumber: String) {
        Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
            startActivity(this)
        }
    }

    private fun chachingOrderImages() {
        for (data in listOfDataFromCameraAndGallery) {
            val submitImagesCacheData = SubmitImagesCacheData(
                orderId = orderId,
                uri = data.uri.toString()
            )
            submitOrderImagesCachingViewModel.insertImage(submitImagesCacheData)
        }
    }

    override fun onDestroyView() {
        job.cancel()
        super.onDestroyView()
        unBindView()
        chachingOrderImages()
    }
}