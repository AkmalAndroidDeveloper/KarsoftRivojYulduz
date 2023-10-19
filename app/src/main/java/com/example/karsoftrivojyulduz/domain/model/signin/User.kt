package com.example.karsoftrivojyulduz.domain.model.signin

data class User(
    val id: Int,
    val name: String,
    val phone: String,
    val role_id: Int,
    val role_name: String
)