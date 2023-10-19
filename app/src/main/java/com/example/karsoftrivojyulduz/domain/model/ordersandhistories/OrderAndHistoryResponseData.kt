package com.example.karsoftrivojyulduz.domain.model.ordersandhistories

data class OrderAndHistoryResponseData(
    val data: List<Data>,
    val message: String,
    val success: Boolean,
    val total: Int
) {
    data class Data(
        val comment: String,
        val contact: Contact,
        val height: Int,
        val id: Int,
        val paid: Boolean,
        val payment_url: String,
        val prepaid_expense: Boolean,
        val quantity: Int,
        val status_id: Int,
        val status_name: String,
        val total_amount: Int,
        val width: Any? = null
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