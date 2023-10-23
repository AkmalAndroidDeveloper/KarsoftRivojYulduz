package com.example.karsoftrivojyulduz.domain.model.submitorder

import android.graphics.Bitmap
import android.net.Uri

data class SubmitImagesData(
    val id: Int,
    val orderId: Int,
    val uri: Uri,
    val image: Bitmap
)
