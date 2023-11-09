package com.example.karsoftrivojyulduz.presentation.ui.submitorder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.databinding.FragmentSubmitOrderBinding
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.Image
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData
import com.example.karsoftrivojyulduz.presentation.ui.dialog.cameraorgallery.CameraOrGalleryChooserDialog
import com.example.karsoftrivojyulduz.presentation.ui.dialog.loading.LoadingDialog
import com.example.karsoftrivojyulduz.presentation.ui.dialog.orderimage.OrderImageDialog
import com.example.karsoftrivojyulduz.presentation.ui.dialog.permission.RequestPermissionDialog
import com.example.karsoftrivojyulduz.presentation.ui.dialog.submitorder.SubmitOrderDialog
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.adapter.HistoryImagesAdapter
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.adapter.SubmitOrderImagesAdapter
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.viewmodel.SubmitOrderImagesCachingViewModel
import com.example.karsoftrivojyulduz.presentation.ui.submitorder.viewmodel.SubmitOrderViewModel
import com.example.karsoftrivojyulduz.util.constant.Constants
import com.example.karsoftrivojyulduz.util.constant.Permission
import com.example.karsoftrivojyulduz.util.convertor.TextFormator
import com.example.karsoftrivojyulduz.util.extension.toastMessage
import com.example.karsoftrivojyulduz.util.local.LocalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SubmitOrderFragment : Fragment(R.layout.fragment_submit_order) {

    private lateinit var cameraOrGalleryChooserDialog: CameraOrGalleryChooserDialog
    private lateinit var submitOrderDialog: SubmitOrderDialog
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var orderImageDialog: OrderImageDialog
    private lateinit var requestPermissionDialog: RequestPermissionDialog
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
    private var hasImagesSelectable: Boolean = false
    private var fromCamera: Boolean = false
    private var fromHistoryFramgnet: Boolean = false
    private var permissionFromCamera: Boolean = false
    private var hasClickedMoreDetails: Boolean = false
    private var orderId: Int = Constants.UNDEFINED_ID
    private var statusId: Int = Constants.UNDEFINED_ID
    private var imageOrderId: Int = 0

    private val submitOrderViewModel by viewModel<SubmitOrderViewModel>()
    private val submitOrderImagesCachingViewModel by viewModel<SubmitOrderImagesCachingViewModel>()
    private val arguments: SubmitOrderFragmentArgs by navArgs()
    private val binding get() = _binding!!

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
        orderImageDialog = OrderImageDialog()
        requestPermissionDialog = RequestPermissionDialog()
    }

    private fun initLists() {
        listOfDataFromCameraAndGallery = mutableListOf()
        listOfSelectedImageForDelete = mutableMapOf()
    }

    private fun updateUI(fromHistoryFramgnet: Boolean) {
        with(binding) {
            tvToolbar.isSelected = true
            if (fromHistoryFramgnet) {
                tvToolbar.text = "${getString(R.string.history_order)} №$orderId"
                ivAddImage.visibility = View.INVISIBLE
                noPhotosContainer.visibility = View.INVISIBLE
                btnSumbitOrDeleteOrder.visibility = View.GONE
            } else {
                tvToolbar.text = "${getString(R.string.submit_order)} №$orderId"
                ivAddImage.visibility = View.VISIBLE
                btnSumbitOrDeleteOrder.visibility = View.INVISIBLE

                if (listOfDataFromCameraAndGallery.isEmpty()) {
                    recyclerViewImages.visibility = View.INVISIBLE
                    noPhotosContainer.visibility = View.VISIBLE
                } else {
                    recyclerViewImages.visibility = View.VISIBLE
                    noPhotosContainer.visibility = View.INVISIBLE
                }
            }
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
        when (statusId) {
            Constants.METER_HAS_COMPLETED_ITS_WORK -> {
                lifecycleScope.launch {
                    submitOrderViewModel.getSimpleOrder(orderId)
                }
            }

            else -> {
                lifecycleScope.launch {
                    submitOrderImagesCachingViewModel.getImages(orderId, statusId)
                    submitOrderImagesCachingViewModel.getImagesSize()
                    submitOrderViewModel.getSimpleOrder(orderId)
                }
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
            this@SubmitOrderFragment.statusId = statusId
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
            var cameraRequestCount = LocalStorage().cameraPermissionCount
            cameraRequestCount++
            LocalStorage().cameraPermissionCount = cameraRequestCount

            if (cameraRequestCount == 3) {
                cameraRequestCount--
                LocalStorage().cameraPermissionCount = cameraRequestCount
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                    cameraResultLauncher.launch(this)
                }
            } else {
                if (isGranted) {
                    openCamera()
                } else {
                    permissionFromCamera = true
                    toastMessage(getString(R.string.permission_denied))
                }
            }
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
            var galleryRequestCount = LocalStorage().galleryPermissionCount
            galleryRequestCount++
            LocalStorage().galleryPermissionCount = galleryRequestCount

            if (galleryRequestCount == 3) {
                galleryRequestCount--
                LocalStorage().galleryPermissionCount = galleryRequestCount
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                    galleryResultLauncher.launch(this)
                }
            } else {
                if (isGranted) {
                    openGallery()
                } else {
                    permissionFromCamera = false
                    toastMessage(getString(R.string.permission_denied))
                }
            }
        }
        galleryResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        )
        {
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
                imageOrderId,
                statusId,
                orderId,
                currentFetchedImageUriFromCamera.toString(),
                currnetFetchedImageFilePathFromCamera ?: ""
            )

            listOfDataFromCameraAndGallery.add(imageData)
            addImageToSubmitOrderImagesAdapterList(imageData)
            checkAndChangeVisibilitySubmitOrderButton(listOfDataFromCameraAndGallery)
//            lifecycleScope.launch {
//                submitOrderImagesCachingViewModel.insertImage(imageData)
//            }
        }
    }

    private fun fetctSelectedImagesFromGallery(it: ActivityResult?) {
        if (it?.data?.clipData != null) {
            val countImages = it.data?.clipData?.itemCount
            if ((countImages?.plus(listOfDataFromCameraAndGallery.size))!! <= 10)
                for (i in 0 until countImages) {
                    if (hasListOfImagesFromCameraAndGalleryLengthMoreThanTen()) toastMessage(
                        getString(R.string.the_number_of_photos_should_not_exceed_ten)
                    )
                    else {
                        incrementImageOrderId()

                        val imageUri = it.data?.clipData?.getItemAt(i)?.uri
                        val imageData = SubmitImagesData(
                            imageOrderId,
                            statusId,
                            orderId,
                            imageUri.toString(),
                            getImageFilePath(
                                requireContext(),
                                imageUri ?: Uri.parse("")
                            ) ?: ""
                        )

                        listOfDataFromCameraAndGallery.add(imageData)
                        addImageToSubmitOrderImagesAdapterList(imageData)
                        checkAndChangeVisibilitySubmitOrderButton(
                            listOfDataFromCameraAndGallery
                        )
//                        lifecycleScope.launch {
//                            submitOrderImagesCachingViewModel.insertImage(imageData)
//                        }
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
                    imageOrderId, statusId, orderId, imageUri.toString(),
                    getImageFilePath(
                        requireContext(),
                        imageUri ?: Uri.parse("")
                    ) ?: ""
                )

                listOfDataFromCameraAndGallery.add(imageData)
                addImageToSubmitOrderImagesAdapterList(imageData)
                checkAndChangeVisibilitySubmitOrderButton(listOfDataFromCameraAndGallery)
//                lifecycleScope.launch {
//                    submitOrderImagesCachingViewModel.insertImage(imageData)
//                }
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

    @SuppressLint("SetTextI18n")
    private fun initObservables(fromHistoryFramgnet: Boolean) {
        clearLists()
        when (statusId) {
            Constants.METER_ATTACHED -> {
                submitOrderImagesCachingViewModel.successImageSize.onEach {
                    imageOrderId = it
                }.launchIn(lifecycleScope)
                submitOrderImagesCachingViewModel.successSubmitOrderImagesFlow.onEach {
                    if (it.isEmpty()) {
                        dismissLoadingDialog()
                        binding.recyclerViewImages.visibility = View.INVISIBLE
                        binding.noPhotosContainer.visibility = View.VISIBLE
                    } else {
                        binding.recyclerViewImages.visibility = View.VISIBLE
                        binding.noPhotosContainer.visibility = View.INVISIBLE

                        listOfDataFromCameraAndGallery.clear()
                        submitOrderImagesAdapter.clearList()

                        listOfDataFromCameraAndGallery.addAll(it)
                        initSubmitOrderImagesAdapterList(listOfDataFromCameraAndGallery)
                        checkAndChangeVisibilitySubmitOrderButton(
                            listOfDataFromCameraAndGallery
                        )
                        dismissLoadingDialog()
                    }
                }.launchIn(lifecycleScope)
            }

            Constants.REDO_THE_WORK -> {
                submitOrderImagesCachingViewModel.successImageSize.onEach {
                    imageOrderId = it
                }.launchIn(lifecycleScope)
                submitOrderImagesCachingViewModel.successSubmitOrderImagesFlow.onEach {
                    if (it.isEmpty()) {
                        dismissLoadingDialog()
                        binding.recyclerViewImages.visibility = View.INVISIBLE
                        binding.noPhotosContainer.visibility = View.VISIBLE

                    } else {
                        binding.recyclerViewImages.visibility = View.VISIBLE
                        binding.noPhotosContainer.visibility = View.INVISIBLE

                        listOfDataFromCameraAndGallery.clear()
                        submitOrderImagesAdapter.clearList()

                        listOfDataFromCameraAndGallery.addAll(it)
                        initSubmitOrderImagesAdapterList(listOfDataFromCameraAndGallery)
                        checkAndChangeVisibilitySubmitOrderButton(
                            listOfDataFromCameraAndGallery
                        )
                        dismissLoadingDialog()
                    }
                }.launchIn(lifecycleScope)
            }

            Constants.METER_HAS_COMPLETED_ITS_WORK -> {
                submitOrderViewModel.successSimpleOrderFlow.onEach {
                    if (fromHistoryFramgnet) initHistoryImagesAdapterList(it.data.images)
                    dismissLoadingDialog()
                }.launchIn(lifecycleScope)
            }
        }
        submitOrderViewModel.successOrderImagesFlow.onEach {
            toastMessage("Потверждено успешно")
            dismissLoadingDialog()
            submitOrderImagesCachingViewModel.deleteImagesByOrderId(orderId, statusId)
            popBackStack()
        }.launchIn(lifecycleScope)
        submitOrderViewModel.successSimpleOrderFlow.onEach {
            with(binding) {
                tvService.text =
                    TextFormator().firstLetterCapitalAndRestAreSmall(it.data.service.title)
                if (it.data.height != null)
                    tvHeight.text = "${it.data.height} ${it.data.service.dimension.unit}"
                else
                    tvHeight.text = "-"
                if (it.data.width != null)
                    tvWidth.text = "${it.data.height} ${it.data.service.dimension.unit}"
                else
                    tvWidth.text = "-"
                tvCount.text = "${it.data.quantity} штук"
            }
        }.launchIn(lifecycleScope)
    }

    private fun showCameraOrGalleryChooserDialog() {
        cameraOrGalleryChooserDialog.show(childFragmentManager, null)
    }

    private fun showSubmitOrderDialog() {
        submitOrderDialog.show(childFragmentManager, SubmitOrderDialog.TAG)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun showOrderImageDialog() {
        if (!orderImageDialog.isVisible)
            orderImageDialog.show(childFragmentManager, OrderImageDialog.TAG)
    }

    private fun showLoadingDialog() {
        loadingDialog.show(childFragmentManager, LoadingDialog.TAG)
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
                                    data.id,
                                    data.statusId,
                                    data.orderId,
                                    data.uri,
                                    data.path
                                )
                                submitOrderImagesCachingViewModel.deleteImage(imageData)
                            }
                        }
                        submitOrderImagesAdapter.deleteImage(listOfSelectedImageForDelete.values.toList())
                        listOfSelectedImageForDelete.clear()

                        hasImagesSelectable = false
                        ivAddImage.visibility = View.VISIBLE
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
                    lifecycleScope.launch {
                        submitOrderViewModel.insertOrderImages(
                            requireContext().contentResolver,
                            orderId,
                            listOfDataFromCameraAndGallery.map { it.uri },
                            requireContext()
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
                            tvToolbar.text = getString(R.string.submit_order)
                            btnSumbitOrDeleteOrder.text =
                                getString(R.string.submit_order_btn)
                        }
                    } else {
                        cardSelect.visibility = View.VISIBLE
                        listOfSelectedImageForDelete[imagePosition] = image
                    }
                } else {
                    orderImageDialog.setImageUri(image.uri.toUri())
                    showOrderImageDialog()
                }
            }
            submitOrderImagesAdapter.setOnLongClickListener { image, hasClick, imagePosition, cardSelect ->
                if (!hasImagesSelectable) {
                    hasImagesSelectable = hasClick
                    ivAddImage.visibility = View.INVISIBLE
                    tvToolbar.text = getString(R.string.viberite_neskolko)
                    btnSumbitOrDeleteOrder.text = getString(R.string.delete)
                    if (!cardSelect.isVisible) {
                        cardSelect.visibility = View.VISIBLE
                        listOfSelectedImageForDelete[imagePosition] = image
                    }
                }
            }
            historyImagesAdapter.setOnItemClickListener { image, imagePosition, _ ->
                orderImageDialog.setImageUrl(image.imageUrl)
                showOrderImageDialog()
            }
            tvCustomerPhoneNumber.setOnClickListener {
                call(tvCustomerPhoneNumber.text.toString())
            }
            loadingDialog.setOnCancelClickListener {
                dismissLoadingDialog()
                popBackStack()
            }
            requestPermissionDialog.onYesButtonClickListener {
                if (permissionFromCamera)
                    Permission().requestCameraPermission(cameraRequestPermissionLauncher)
                else
                    Permission().requestGalleryPermission(
                        requireContext(),
                        galleryRequestPermissionLauncher,
                        galleryResultLauncher
                    )

            }
            ivMoreDetails.setOnClickListener {
                if (hasClickedMoreDetails) {
                    hasClickedMoreDetails = false
                    ivMoreDetails.rotation = 0f
                    binding.orderDataContainer.visibility = View.GONE
                } else {
                    hasClickedMoreDetails = true
                    ivMoreDetails.rotation = 180f
                    binding.orderDataContainer.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun checkAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (Permission().hasReadMediaImagesPermission(requireContext())) {
                openGallery()
            } else {
                Permission().requestGalleryPermission(
                    requireContext(),
                    galleryRequestPermissionLauncher,
                    galleryResultLauncher
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Permission().hasManageExternalStoragePermission()) {
                openGallery()
            } else {
                Permission().requestGalleryPermission(
                    requireContext(),
                    galleryRequestPermissionLauncher,
                    galleryResultLauncher
                )
            }
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (Permission().hasReadExternalStoragePermission(requireContext())) {
                openGallery()
            } else {
                Permission().requestGalleryPermission(
                    requireContext(),
                    galleryRequestPermissionLauncher,
                    galleryResultLauncher
                )
            }
        }
    }

    private fun checkAndOpenCamera() {
        if (Permission().hasCameraPermission(requireContext())) openCamera()
        else Permission().requestCameraPermission(cameraRequestPermissionLauncher)
    }

    private fun incrementImageOrderId() {
        ++imageOrderId
    }

    private fun popBackStack() {
        findNavController().popBackStack()
    }

    @SuppressLint("IntentReset")
    private fun openGallery() {
        val intent = Intent(
            Intent.ACTION_GET_CONTENT,
            Images.Media.EXTERNAL_CONTENT_URI
        )
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
                cameraIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    currentFetchedImageUriFromCamera
                )
                cameraResultLauncher.launch(cameraIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun call(phoneNumber: String) {
        Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
            startActivity(this)
        }
    }

    private fun createImageFile(): File? {
        val date = SimpleDateFormat("yyyy_MM_dd", Locale.getDefault()).format(Date())
        val imageFileName = "ORDER_IMG_${date}_${System.currentTimeMillis()}"
        val storageDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera")
        val imageFile = File.createTempFile(
            imageFileName, Constants.JPG, storageDirectory
        )
        currnetFetchedImageFilePathFromCamera = imageFile.absolutePath
        return imageFile
    }

    private fun clearLists() {
        listOfDataFromCameraAndGallery.clear()
        listOfSelectedImageForDelete.clear()
    }

    @SuppressLint("Range", "Recycle")
    private fun getImageFilePath(context: Context, uri: Uri?): String? {
        val path: String?
        var cursor: Cursor? = null
        try {
            cursor =
                requireContext().contentResolver.query(
                    uri!!,
                    arrayOf(Images.Media.DATA),
                    null,
                    null,
                    null
                )
            cursor!!.moveToFirst()
            path = cursor.getString(cursor.getColumnIndex(Images.Media.DATA))
        } finally {
            cursor!!.close()
        }
        return path
    }

    private fun checkAndChangeVisibilitySubmitOrderButton(list: List<*>) {
        if (list.isEmpty())
            binding.btnSumbitOrDeleteOrder.visibility = View.INVISIBLE
        else
            binding.btnSumbitOrDeleteOrder.visibility = View.VISIBLE

    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBindView()
        clearLists()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}