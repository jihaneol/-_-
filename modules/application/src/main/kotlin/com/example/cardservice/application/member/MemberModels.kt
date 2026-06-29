package com.example.cardservice.application.member

import com.example.cardservice.domain.member.MemberRole

data class CreateMemberInput(
    val username: String,
    val password: String,
    val name: String?,
    val email: String?,
    val role: MemberRole = MemberRole.USER,
)
data class UpdateMemberInput(val name: String?, val email: String?)
data class MemberResult(val id: Long, val username: String, val name: String, val email: String?, val role: MemberRole)
data class MemberPageResult(
    val items: List<MemberResult>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)
data class LoginInput(val username: String, val password: String)
data class AuthenticatedMemberResult(
    val id: Long,
    val username: String,
    val name: String,
    val email: String?,
    val role: MemberRole,
)
