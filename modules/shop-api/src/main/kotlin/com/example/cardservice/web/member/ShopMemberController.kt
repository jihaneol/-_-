package com.example.cardservice.web.member

import com.example.cardservice.application.member.CreateMemberRequest
import com.example.cardservice.application.member.MemberResponse
import com.example.cardservice.application.member.required.MemberUseCase
import com.example.cardservice.domain.member.MemberRole
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
@RequestMapping("/api/shop/members")
@Tag(name = "Shop Member", description = "쇼핑몰 회원 API")
class ShopMemberController(
    private val memberUseCase: MemberUseCase,
) {
    @PostMapping
    @Operation(summary = "쇼핑몰 데모 회원 가입")
    fun signup(@RequestBody request: CreateMemberRequest): ResponseEntity<ApiResponse<MemberResponse>> =
        memberUseCase
            .createMember(
                request.copy(role = MemberRole.USER),
            )
            .toApplicationResponse(ApplicationResponseType.CREATED)
}
