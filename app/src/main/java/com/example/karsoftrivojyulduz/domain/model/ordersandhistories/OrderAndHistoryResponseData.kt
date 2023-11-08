package com.example.karsoftrivojyulduz.domain.model.ordersandhistories

import com.google.gson.annotations.SerializedName

data class OrderAndHistoryResponseData(
    val data: List<Data>,
    val message: String,
    val success: Boolean,
    val total: Int
) {
    data class Data(
        val comment: String,
        val contact: Contact,
        val height: Long,
        val id: Int,
        val paid: Boolean,
        @SerializedName("payment_url")
        val paymentUrl: String,
        @SerializedName("prepaid_expense")
        val prepaidExpense: Boolean,
        val quantity: Int,
        @SerializedName("status_id")
        val statusId: Int,
        @SerializedName("status_name")
        val statusName: String,
        @SerializedName("total_amount")
        val totalAmount: Long,
        val width: Any? = null,
        @SerializedName("created_at")
        val createdAt: String,
        @SerializedName("submitted_at")
        val submittedAt: String
    )

    data class Contact(
        val address: Any? = null,
        val comment: String,
        val id: Int,
        val name: String,
        val phone: String,
        val title: Any? = null
    )
}