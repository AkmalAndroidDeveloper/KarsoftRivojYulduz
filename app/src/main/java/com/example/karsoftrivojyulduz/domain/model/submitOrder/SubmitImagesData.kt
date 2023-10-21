package com.example.karsoftrivojyulduz.domain.model.submitOrder

import android.graphics.Bitmap

data class SubmitImagesData(
    val id: Int,
    val orderId: Int,
    val image: Bitmap
)
