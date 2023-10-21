package com.example.karsoftrivojyulduz.util.validator

class PasswordValidator(private val password: String) {
    fun hasLengthEmpty() = password.isEmpty()

    fun hasEnteredMoreValueThanRequired() = password.length > 50

    fun hasCyrillicLetters(): Boolean {
        val pattern = "[a-zA-Z0-9!\"#$%&'()*+,\\-./:;<>=?@\\[\\]{}\\\\^_`~]+$"
        return !Regex(pattern).matches(password)
    }
}