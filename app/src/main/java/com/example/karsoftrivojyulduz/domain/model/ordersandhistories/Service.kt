package com.example.karsoftrivojyulduz.domain.model.ordersandhistories

data class Service(
    val description: String,
    val dimension: Dimension,
    val each: Int,
    val is_discount: Boolean,
    val is_public: Boolean,
    val price: Int,
    val price_each: Int,
    val slug: String,
    val title: String
)