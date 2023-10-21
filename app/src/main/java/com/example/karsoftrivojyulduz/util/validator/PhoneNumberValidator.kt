package com.example.karsoftrivojyulduz.util.validator

class PhoneNumberValidator(private val phoneNumber: String) {
    fun hasLengthEmpty() = phoneNumber.isEmpty()
    fun hasEnteredLessValueThanRequired() = phoneNumber.length < 12
}