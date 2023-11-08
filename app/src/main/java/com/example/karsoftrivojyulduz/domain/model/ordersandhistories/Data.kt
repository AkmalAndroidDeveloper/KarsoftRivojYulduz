package com.example.karsoftrivojyulduz.domain.model.ordersandhistories

import com.google.gson.annotations.SerializedName

data class Data(
    val comment: String,
    val contact: Contact,
    val details: List<Detail>,
    val height: Int?,
    val id: Int,
    val images: List<Image>,
    val paid: Boolean,
    @SerializedName("payment_url")
    val paymentUrl: String,
    @SerializedName("prepaid_expense")
    val prepaidExpense: Boolean,
    val quantity: Int,
    val service: Service,
    @SerializedName("status_id")
    val statusId: Int,
    @SerializedName("status_name")
    val statusName: String,
    @SerializedName("total_amount")
    val totalAmount: Int,
    val user: User,
    val width: Any? = null
)