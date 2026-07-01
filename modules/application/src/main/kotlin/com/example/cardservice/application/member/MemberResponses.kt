package com.example.cardservice.application.member

import com.example.cardservice.domain.member.MemberRole

data class MemberResponse(val id: Long, val username: String, val name: String, val email: String?, val role: MemberRole)

data class MemberPageResponse(
    val items: List<MemberResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class AuthenticatedMemberResponse(
    val id: Long,
    val username: String,
    val name: String,
    val email: String?,
    val role: MemberRole,
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
