package com.example.karsoftrivojyulduz.domain.model.ordersandhistories

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val name: String,
    val phone: String,
    @SerializedName("role_id")
    val roleId: Int,
    @SerializedName("role_name")
    val roleName: String
)