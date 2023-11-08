package com.example.karsoftrivojyulduz.util.convertor

class TextFormator {

    fun firstLetterCapitalAndRestAreSmall(text: String): String {
        val firstChar = text.substring(0, 1).uppercase()
        val lastChars = text.substring(1).lowercase()
        return "$firstChar$lastChars"
    }
}