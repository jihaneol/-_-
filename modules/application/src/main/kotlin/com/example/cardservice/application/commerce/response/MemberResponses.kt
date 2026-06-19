package com.example.cardservice.application.commerce.response

import com.example.cardservice.application.commerce.MemberResult

data class MemberResponse(val id: Long, val name: String, val email: String)

fun MemberResult.toResponse(): MemberResponse = MemberResponse(id, name, email)
