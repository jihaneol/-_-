package com.example.cardservice.application.member

import com.example.cardservice.domain.member.MemberRole

data class CreateMemberRequest(
    val username: String,
    val password: String,
    val name: String?,
    val email: String?,
    val role: MemberRole = MemberRole.USER,
)

data class UpdateMemberRequest(val name: String?, val email: String?) {
    var memberId: Long = 0L
}

data class LoginRequest(val username: String, val password: String)
