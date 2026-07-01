package com.example.cardservice.web.auth

import com.example.cardservice.application.member.AuthMemberResponse
import com.example.cardservice.application.member.AuthResponse
import com.example.cardservice.application.member.CreateMemberRequest
import com.example.cardservice.application.member.LoginRequest
import com.example.cardservice.application.member.required.MemberAuthUseCase
import com.example.cardservice.application.member.required.MemberUseCase
import com.example.cardservice.domain.member.MemberRole
import com.example.cardservice.security.JwtTokenProvider
import com.example.cardservice.web.common.ApplicationResponseType
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
@RequestMapping("/api/shop/auth")
@Tag(name = "Shop Auth", description = "사용자 인증 API")
class ShopAuthController(
    private val memberUseCase: MemberUseCase,
    private val memberAuthUseCase: MemberAuthUseCase,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    @PostMapping("/signup")
    @Operation(summary = "사용자 가입 및 토큰 발급")
    fun signup(@RequestBody request: CreateMemberRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val member = memberUseCase.createMember(
            request.copy(role = MemberRole.USER),
        )

        return AuthResponse(
            accessToken = jwtTokenProvider.createToken(member.username, member.role),
            member = AuthMemberResponse(member.id, member.username, member.name, member.email, member.role),
        ).toApplicationResponse(ApplicationResponseType.CREATED)
    }

    @PostMapping("/login")
    @Operation(summary = "사용자 로그인")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val member = memberAuthUseCase.authenticate(request)
        require(member.role == MemberRole.USER) { "사용자 권한이 필요합니다." }

        return AuthResponse(
            accessToken = jwtTokenProvider.createToken(member.username, member.role),
            member = AuthMemberResponse(member.id, member.username, member.name, member.email, member.role),
        ).toApplicationResponse()
    }
}
