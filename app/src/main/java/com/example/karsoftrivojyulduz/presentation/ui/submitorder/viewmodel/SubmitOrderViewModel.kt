package com.example.karsoftrivojyulduz.presentation.ui.submitorder.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.karsoftrivojyulduz.data.repository.OrderImagesRepositoryImpl
import com.example.karsoftrivojyulduz.data.repository.SimpleOrderRepositoryImpl
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderImagesRequestData
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderImagesResponseData
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.SimpleOrderResponseData
import com.example.karsoftrivojyulduz.domain.usecase.impl.OrderImagesUseCaseImpl
import com.example.karsoftrivojyulduz.domain.usecase.impl.SimpleOrderUseCaseImpl
import com.example.karsoftrivojyulduz.util.convertor.JSONObjectConvertor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File

class SubmitOrderViewModel(
    private val orderImagesRepositoryImpl: OrderImagesRepositoryImpl,
    private val simpleOrderRepositoryImpl: SimpleOrderRepositoryImpl
) : ViewModel() {

    // insert order images
    private val _successOrderImagesFlow = MutableSharedFlow<OrderImagesResponseData>()
    val successOrderImagesFlow: Flow<OrderImagesResponseData> get() = _successOrderImagesFlow

    private val _messageOrderImagesFlow = MutableSharedFlow<String>()
    val messageOrderImagesFlow: Flow<String> get() = _messageOrderImagesFlow

    private val _errorOrderImagesFlow = MutableSharedFlow<String>()
    val errorOrderImagesFlow: Flow<String> get() = _errorOrderImagesFlow


    // get simple order
    private val _successSimpleOrderFlow = MutableSharedFlow<SimpleOrderResponseData>()
    val successSimpleOrderFlow: Flow<SimpleOrderResponseData> get() = _successSimpleOrderFlow

    private val _messageSimpleOrderFlow = MutableSharedFlow<String>()
    val messageSimpleOrderFlow: Flow<String> get() = _messageSimpleOrderFlow

    private val _errorSimpleOrderFlow = MutableSharedFlow<String>()
    val errorSimpleOrderFlow: Flow<String> get() = _errorSimpleOrderFlow

    suspend fun insertOrderImages(
        contentResolver: ContentResolver,
        orderId: Int,
        images: List<String>,
        context: Context
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val orderImagesUseCaseImpl = OrderImagesUseCaseImpl(orderImagesRepositoryImpl)

            val orderId = RequestBody.create(MultipartBody.FORM, orderId.toString())
            val description = RequestBody.create(MultipartBody.FORM, "Фотографии заказа №$orderId")
            val imagesMultiPart = mutableListOf<MultipartBody.Part>()
            images.map {
                imagesMultiPart.add(
                    convertUriToMultipartBodyPart(
                        it.toUri(),
                        contentResolver,
                        context
                    )
                )
                Log.d("VIEWMODEL", "imagesMultiPart: $imagesMultiPart")
            }
            val orderImagesRequestData =
                OrderImagesRequestData(orderId, description, imagesMultiPart)

            orderImagesUseCaseImpl.execute(orderImagesRequestData).catch {
                it.printStackTrace()
                _errorOrderImagesFlow.emit(it.localizedMessage ?: "")
            }.collect {
                if (it.isSuccessful) it.body()?.let { data -> _successOrderImagesFlow.emit(data) }
                else _messageOrderImagesFlow.emit(
                    JSONObjectConvertor().convertErrorObjectToMessage(it) ?: ""
                )
            }
        }
    }

    suspend fun getSimpleOrder(orderId: Int) {
        viewModelScope.launch {
            val simpleOrderUseCaseImpl = SimpleOrderUseCaseImpl(simpleOrderRepositoryImpl)

            simpleOrderUseCaseImpl.execute(orderId).catch {
                it.printStackTrace()
                _errorSimpleOrderFlow.emit(it.localizedMessage ?: "")
            }.collect {
                if (it.isSuccessful) it.body()?.let { data -> _successSimpleOrderFlow.emit(data) }
                else {
                    _messageSimpleOrderFlow.emit(
                        JSONObjectConvertor().convertErrorObjectToMessage(
                            it
                        ) ?: ""
                    )
                }
            }
        }
    }

    private fun convertUriToMultipartBodyPart(
        uri: Uri?, contentResolver: ContentResolver, context: Context
    ): MultipartBody.Part {
//        val bitmap = Images.Media.getBitmap(contentResolver, uri)
//        val contentResolver = context.contentResolver
//        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//        contentResolver.takePersistableUriPermission(uri ?: Uri.parse(""), takeFlags)

        val inputStream = contentResolver.openInputStream(uri!!)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val byteArrayOutpuStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutpuStream)
        val byteArray = byteArrayOutpuStream.toByteArray()
        val image = RequestBody.create("image/*".toMediaTypeOrNull(), byteArray)
        return MultipartBody.Part.createFormData("images[]", File(uri.path!!).name, image)
    }
}
