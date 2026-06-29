package com.example.cardservice.application.member.request

import com.example.cardservice.domain.member.MemberRole

data class MemberRequest(
    val username: String,
    val password: String,
    val name: String? = null,
    val email: String? = null,
    val role: MemberRole? = null,
)

data class UpdateMemberRequest(
    val name: String? = null,
    val email: String? = null,
)

data class LoginRequest(
    val username: String,
    val password: String,
)

data class AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val member: AuthMemberResponse,
)

data class AuthMemberResponse(
    val id: Long,
    val username: String,
    val name: String,
    val email: String?,
    val role: MemberRole,
)
