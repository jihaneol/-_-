package com.example.cardservice.application.member.provided

interface PasswordHashPort {
    fun encode(rawPassword: String): String
    fun matches(rawPassword: String, encodedPassword: String): Boolean
}
