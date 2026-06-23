package com.example.cardservice.web.shop

import com.example.cardservice.application.commerce.CreateMemberInput
import com.example.cardservice.application.commerce.MemberResult
import com.example.cardservice.application.commerce.request.MemberRequest
import com.example.cardservice.application.commerce.required.MemberUseCase
import com.example.cardservice.web.common.ApiResponse
import com.example.cardservice.web.common.created
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
    fun signup(@RequestBody request: MemberRequest): ResponseEntity<ApiResponse<MemberResult>> =
        created(memberUseCase.createMember(CreateMemberInput(request.name, request.email)))
}
