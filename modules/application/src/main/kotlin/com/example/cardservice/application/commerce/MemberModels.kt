package com.example.cardservice.application.commerce

data class CreateMemberInput(val name: String, val email: String)
data class UpdateMemberInput(val name: String, val email: String)
data class MemberResult(val id: Long, val name: String, val email: String)
