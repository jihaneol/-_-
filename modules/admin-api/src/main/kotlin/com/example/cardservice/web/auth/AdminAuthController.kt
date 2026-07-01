package com.example.cardservice.web.auth

import com.example.cardservice.application.member.AuthMemberResponse
import com.example.cardservice.application.member.AuthResponse
import com.example.cardservice.application.member.LoginRequest
import com.example.cardservice.application.member.required.MemberAuthUseCase
import com.example.cardservice.domain.member.MemberRole
import com.example.cardservice.security.JwtTokenProvider
import com.example.cardservice.web.common.ApiResponse
import com.example.cardservice.web.common.toApplicationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/auth")
@Tag(name = "Admin Auth", description = "관리자 인증 API")
class AdminAuthController(
    private val memberAuthUseCase: MemberAuthUseCase,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    @PostMapping("/login")
    @Operation(summary = "관리자 로그인")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val member = memberAuthUseCase.authenticate(request)
        require(member.role == MemberRole.ADMIN) { "관리자 권한이 필요합니다." }

        return AuthResponse(
            accessToken = jwtTokenProvider.createToken(member.username, member.role),
            member = AuthMemberResponse(member.id, member.username, member.name, member.email, member.role),
        ).toApplicationResponse()
    }
}
