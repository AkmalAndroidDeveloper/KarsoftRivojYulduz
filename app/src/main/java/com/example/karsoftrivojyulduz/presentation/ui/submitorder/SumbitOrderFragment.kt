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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.databinding.FragmentSubmitOrderBinding
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.Image
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderImagesRequestData
import com.example.karsoftrivojyulduz.domain.model.submitOrder.SubmitImagesCacheData
import com.example.karsoftrivojyulduz.domain.model.submitOrder.SubmitImagesData
import com.example.karsoftrivojyulduz.presentation.ui.dialog.cameraorgallery.CameraOrGalleryChooserDialog
import com.example.karsoftrivojyulduz.presentation.ui.dialog.loading.LoadingDialog
import com.example.karsoftrivojyulduz.presentation.ui.dialog.submitorder.SubmitOrderDialog
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.adapter.HistoryImagesAdapter
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.adapter.SubmitOrderImagesAdapter
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.viewmodel.SubmitOrderImagesCachingViewModel
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.viewmodel.SubmitOrderViewModel
import com.example.karsoftrivojyulduz.util.constant.Constants
import com.example.karsoftrivojyulduz.util.convertor.ImageConvertor
import com.example.karsoftrivojyulduz.util.extension.toastMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream
import java.io.File


class SumbitOrderFragment : Fragment(R.layout.fragment_submit_order) {

    private lateinit var submitOrderImagesAdapter: SubmitOrderImagesAdapter
    private lateinit var historyImagesAdapter: HistoryImagesAdapter
    private lateinit var cameraRequestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryRequestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var galleryResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraOrGalleryChooserDialog: CameraOrGalleryChooserDialog
    private lateinit var submitOrderDialog: SubmitOrderDialog
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var listOfImagesFromCameraAndGallery: MutableList<SubmitImagesData>
    private lateinit var listOfImageUrisFromCameraAndGallery: MutableList<Uri>
    private lateinit var listOfSelectedImageForDelete: MutableMap<Int, String>

    private var _binding: FragmentSubmitOrderBinding? = null
    private var hasImagesSelectable: Boolean = false
    private var fromCamera: Boolean = false
    private var fromHistoryFramgnet = false
    private var currentImagePathFromCamera: String? = null
    private var orderId: Int = Constants.UNDEFINED_ID
    private var imageOrderId: Int = 0
    private var imageId: Int = 0

    private val binding get() = _binding!!
    private val arguments: SumbitOrderFragmentArgs by navArgs()
    private val submitOrderViewModel by viewModel<SubmitOrderViewModel>()
    private val submitOrderImagesCachingViewModel by viewModel<SubmitOrderImagesCachingViewModel>()

    companion object {
        private const val TAG = "SumbitOrderFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initRegistersForActivityResult()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView(view)

        initValues()
        initLists()
        initAdapters()
        initDialogs()
        getData()
        updateUI(fromHistoryFramgnet)
        initObservables(fromHistoryFramgnet)
        initListeners()
    }

    private fun initDialogs() {
        loadingDialog = LoadingDialog()
        submitOrderDialog = SubmitOrderDialog()
        cameraOrGalleryChooserDialog = CameraOrGalleryChooserDialog()
    }

    private fun initLists() {
        listOfImagesFromCameraAndGallery = mutableListOf()
        listOfImageUrisFromCameraAndGallery = mutableListOf()
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

                if (listOfImagesFromCameraAndGallery.isEmpty()) {
                    recyclerViewImages.visibility = View.INVISIBLE
                    noPhotosContainer.visibility = View.VISIBLE
                } else {
                    recyclerViewImages.visibility = View.VISIBLE
                    noPhotosContainer.visibility = View.INVISIBLE
                }
            }
            tvToolbarNumberCounter.text = "№$orderId"
        }
    }

    private fun getData() {
        showLoadingDialog()
        if (!fromHistoryFramgnet) {
            lifecycleScope.launch {
                submitOrderImagesCachingViewModel.getAllImages(orderId)
            }
        } else {
            lifecycleScope.launch {
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
        orderId = arguments.orderId
        fromHistoryFramgnet = arguments.fromHistoryFragment
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
            if (it.resultCode == Activity.RESULT_OK)
                lifecycleScope.launch {
                fromCamera = true
                fetchImageFromCamera(it)
                checkAndChangeVisibilityIfListOfImagesFromCameraAndGalleryIsNotEmpty()
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
                lifecycleScope.launch {
                    fromCamera = false
                    fetctSelectedImagesFromGallery(it)
                    checkAndChangeVisibilityIfListOfImagesFromCameraAndGalleryIsNotEmpty()
                    if (!fromHistoryFramgnet)
                        cachingOrderImages()
                }
            }
        }
    }

    private fun fetchImageFromCamera(activityResult: ActivityResult) {
        val bitmap = BitmapFactory.decodeFile(currentImagePathFromCamera)

        if (hasListOfImagesFromCameraAndGalleryLengthMoreThanTen()) toastMessage(getString(R.string.the_number_of_photos_should_not_exceed_ten))
        else {
            incrementImageOrderId()

            val imageData = SubmitImagesData(imageId, orderId, bitmap)

            listOfImageUrisFromCameraAndGallery.add(
                getImageUri(requireContext(), bitmap) ?: Uri.parse("")
            )
            listOfImagesFromCameraAndGallery.add(imageData)
            addImageToSubmitOrderImagesAdapterList(imageData)
            saveImage(bitmap, "order_${orderId}_$imageOrderId")
        }
    }

    private fun fetctSelectedImagesFromGallery(it: ActivityResult?) {
        if (it?.data?.clipData != null) {
            val countImages = it.data?.clipData?.itemCount

            if ((countImages?.plus(listOfImagesFromCameraAndGallery.size))!! <= 10)
                for (i in 0 until countImages) {

                    val imageUri = it.data?.clipData?.getItemAt(i)?.uri
                    val bitmap = Images.Media.getBitmap(requireContext().contentResolver, imageUri)

                    if (hasListOfImagesFromCameraAndGalleryLengthMoreThanTen())
                        toastMessage(
                            getString(R.string.the_number_of_photos_should_not_exceed_ten)
                        )
                    else {
                        incrementImageOrderId()

                        val imageData = SubmitImagesData(imageId, orderId, bitmap)

                        listOfImageUrisFromCameraAndGallery.add(imageUri ?: Uri.parse(""))
                        listOfImagesFromCameraAndGallery.add(imageData)
                        addImageToSubmitOrderImagesAdapterList(imageData)
                    }
                }
            else toastMessage(getString(R.string.the_number_of_photos_should_not_exceed_ten))

        } else if (it?.data?.data != null) {
            val imageUri = it.data?.data
            val bitmap = Images.Media.getBitmap(requireContext().contentResolver, imageUri)

            if (hasListOfImagesFromCameraAndGalleryLengthMoreThanTen()) {
                toastMessage(getString(R.string.the_number_of_photos_should_not_exceed_ten))
            } else {
                incrementImageOrderId()

                val imageData = SubmitImagesData(imageId, orderId, bitmap)

                listOfImageUrisFromCameraAndGallery.add(imageUri ?: Uri.parse(""))
                listOfImagesFromCameraAndGallery.add(imageData)
                addImageToSubmitOrderImagesAdapterList(imageData)
            }
        }
    }

    private fun checkAndChangeVisibilityIfListOfImagesFromCameraAndGalleryIsNotEmpty() {
        if (listOfImagesFromCameraAndGallery.isNotEmpty()) {
            binding.recyclerViewImages.visibility = View.VISIBLE
            binding.noPhotosContainer.visibility = View.INVISIBLE
        }
    }

    private fun hasListOfImagesFromCameraAndGalleryLengthMoreThanTen() =
        listOfImagesFromCameraAndGallery.size > 10

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
        if (!fromHistoryFramgnet)
            with(submitOrderImagesCachingViewModel) {
                successSubmitOrderImagesFlow.onEach {
                    dismissLoadingDialog()
                    if (it.isNotEmpty()) {
                        binding.noPhotosContainer.visibility = View.INVISIBLE
                        binding.recyclerViewImages.visibility = View.VISIBLE

                        for (data in it) {
                            val id = data.id ?: Constants.UNDEFINED_ID
                            val orderId = data.orderId
                            val path = Uri.parse(data.path)
                            val bitmap = ImageConvertor().stringToBitmap(data.image)
                            val submitImagesData = SubmitImagesData(id, orderId, bitmap!!)

                            listOfImagesFromCameraAndGallery.add(submitImagesData)
                            listOfImageUrisFromCameraAndGallery.add(path)

                            addImageToSubmitOrderImagesAdapterList(submitImagesData)
                            setUpRecyclerViewLayoutManager()
                            setUpRecyclerViewAdapter(fromHistoryFramgnet)
                        }
                    } else {
                        binding.noPhotosContainer.visibility = View.VISIBLE
                        binding.recyclerViewImages.visibility = View.INVISIBLE

                    }
                }.launchIn(lifecycleScope)
            }
        else
            with(submitOrderViewModel) {
                successSimpleOrderFlow.onEach {
                    toastMessage("success")
                    dismissLoadingDialog()
                    with(binding) {
                        tvTitle.text = it.data.contact.title.toString()
                        tvLocation.text = it.data.contact.address.toString()
                        tvCustomerName.text = it.data.contact.name
                        tvCustomerPhoneNumber.text = it.data.contact.phone

                        if (fromHistoryFramgnet) initHistoryImagesAdapterList(it.data.images)

                        setUpRecyclerViewLayoutManager()
                        setUpRecyclerViewAdapter(fromHistoryFramgnet)
                    }
                }.launchIn(lifecycleScope)
                messageSimpleOrderFlow.onEach {
                    toastMessage("message")
                    Log.d(TAG, "Message: $it")
                }.launchIn(lifecycleScope)
                errorSimpleOrderFlow.onEach {
                    toastMessage("error")
                    Log.d(TAG, "Error: $it")
                }.launchIn(lifecycleScope)

                successOrderImagesFlow.onEach {
                    Log.d(TAG, "Success: ${it.message}")
                    dismissLoadingDialog()
                    findNavController().popBackStack()
                }.launchIn(lifecycleScope)
                messageOrderImagesFlow.onEach {
                    Log.d(TAG, "Message: $it")
                    dismissLoadingDialog()
                }.launchIn(lifecycleScope)
                errorOrderImagesFlow.onEach {
                    Log.d(TAG, "Error: $it")
                    dismissLoadingDialog()
                    popBackStack()
                }.launchIn(lifecycleScope)
            }
    }

    private fun dismissLoadingDialog() {
        loadingDialog.dismiss()
    }

    private fun addImageToSubmitOrderImagesAdapterList(imageData: SubmitImagesData) {
        submitOrderImagesAdapter.addImage(imageData)
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
                showCameraOrGalleryChooserDialog()
            }
            btnSumbitOrDeleteOrder.setOnClickListener {
                if (hasImagesSelectable) {
                    if (listOfSelectedImageForDelete.isNotEmpty()) {
                        for (i in listOfSelectedImageForDelete.keys) {
                            listOfImageUrisFromCameraAndGallery.removeAt(i)
                            listOfImagesFromCameraAndGallery.removeAt(i)
                        }
                        hasImagesSelectable = false
                        ivAddImage.visibility = View.VISIBLE
                        tvToolbarNumberCounter.visibility = View.VISIBLE
                        tvToolbar.text = getString(R.string.submit_order)
                        btnSumbitOrDeleteOrder.text = getString(R.string.submit_order_btn)
                    } else {
                        toastMessage(getString(R.string.choose_photo_for_delete))
                    }
                } else showSubmitOrderDialog()
            }
            cameraOrGalleryChooserDialog.onCameraClickListener {
                checkAndOpenCamera()
            }
            cameraOrGalleryChooserDialog.onGalleryClickListener {
                checkAndOpenGallery()
            }
            submitOrderDialog.onYesButtonClickListener {
                val orderId = RequestBody.create(MultipartBody.FORM, orderId.toString())
                val description =
                    RequestBody.create(MultipartBody.FORM, "Фотографии заказа №$orderId")
                val images = listOfImageUrisFromCameraAndGallery.map { uri ->
                    (convertUriToMultipartBodyPart(uri, requireActivity().contentResolver))
                }

                if (images.isEmpty()) {
                    toastMessage("Добавьте фотографии")
                } else {
                    lifecycleScope.launch {
                        submitOrderViewModel.insertOrderImages(
                            OrderImagesRequestData(
                                orderId, description, images
                            )
                        )
                    }
                    showLoadingDialog()
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
                        SumbitOrderFragmentDirections.actionOrderFragmentToOrderImageFragment(
                            getImageUri(requireContext(), image.image).toString(),
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
            historyImagesAdapter.setOnItemClickListener { image, imagePosition, cardSelect ->
                val direction =
                    SumbitOrderFragmentDirections.actionOrderFragmentToOrderImageFragment(
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
            openCamera("order_${orderId}_${incrementImageOrderId()}")
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutpuStream)
        val byteArray = byteArrayOutpuStream.toByteArray()
        val image = RequestBody.create("image/*".toMediaTypeOrNull(), byteArray)
        return MultipartBody.Part.createFormData("images[]", File(uri?.path ?: "").name, image)
    }

    private fun getImageUri(context: Context, bitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes)
        val path = Images.Media.insertImage(context.contentResolver, bitmap, "", null)
        return Uri.parse(path)
    }

    private fun navigateTo(direction: NavDirections) {
        findNavController().navigate(direction)
    }

    private fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
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
        var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        galleryResultLauncher.launch(intent)
    }

    private fun openCamera(fileName: String) {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val storageDirectory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
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

    private fun cachingOrderImages() {
        lifecycleScope.launch {
            for (i in listOfImagesFromCameraAndGallery.indices) {
                val bitmapString = ImageConvertor().bitmapToString(listOfImagesFromCameraAndGallery[i].image) ?: ""
                val path = listOfImageUrisFromCameraAndGallery[i].toString()
                val submitOrderCacheData = SubmitImagesCacheData(orderId = orderId, image = bitmapString, path = path)
                submitOrderImagesCachingViewModel.insertImage(submitOrderCacheData)
                Log.d(TAG, "cachingOrderImages: $submitOrderCacheData")
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBindView()
        listOfImagesFromCameraAndGallery.clear()
        listOfImageUrisFromCameraAndGallery.clear()
        listOfSelectedImageForDelete.clear()
    }
}