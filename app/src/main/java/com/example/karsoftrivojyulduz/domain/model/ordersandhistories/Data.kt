package com.example.karsoftrivojyulduz.domain.model.ordersandhistories

data class Data(
    val comment: String,
    val contact: Contact,
    val details: List<Detail>,
    val height: Int,
    val id: Int,
    val images: List<Image>,
    val paid: Boolean,
    val payment_url: String,
    val prepaid_expense: Boolean,
    val quantity: Int,
    val service: Service,
    val status_id: Int,
    val status_name: String,
    val total_amount: Int,
    val user: User,
    val width: Any? = null
)