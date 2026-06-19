package com.example.cardservice.web.commerce

import com.example.cardservice.application.commerce.CreateMemberInput
import com.example.cardservice.application.commerce.UpdateMemberInput
import com.example.cardservice.application.commerce.request.MemberRequest
import com.example.cardservice.application.commerce.required.MemberQueryUseCase
import com.example.cardservice.application.commerce.required.MemberUseCase
import com.example.cardservice.application.commerce.response.toResponse
import com.example.cardservice.web.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/members")
@Tag(name = "Member", description = "회원 운영 API")
class MemberController(
    private val memberUseCase: MemberUseCase,
    private val memberQueryUseCase: MemberQueryUseCase,
) {
    @PostMapping
    @Operation(summary = "회원 생성")
    fun createMember(@RequestBody request: MemberRequest): ResponseEntity<ApiResponse<Any>> =
        created(memberUseCase.createMember(CreateMemberInput(request.name, request.email)).toResponse())

    @GetMapping
    @Operation(summary = "회원 목록 조회")
    fun listMembers(): ApiResponse<Any> =
        ApiResponse.success(memberQueryUseCase.listMembers().map { it.toResponse() })

    @GetMapping("/{memberId}")
    @Operation(summary = "회원 상세 조회")
    fun getMember(@PathVariable memberId: Long): ApiResponse<Any> =
        ApiResponse.success(memberQueryUseCase.getMember(memberId).toResponse())

    @PatchMapping("/{memberId}")
    @Operation(summary = "회원 수정")
    fun updateMember(
        @PathVariable memberId: Long,
        @RequestBody request: MemberRequest,
    ): ApiResponse<Any> =
        ApiResponse.success(memberUseCase.updateMember(memberId, UpdateMemberInput(request.name, request.email)).toResponse())

    @DeleteMapping("/{memberId}")
    @Operation(summary = "회원 소프트 삭제")
    fun deleteMember(@PathVariable memberId: Long): ResponseEntity<Void> {
        memberUseCase.deleteMember(memberId)
        return ResponseEntity.noContent().build()
    }

    private fun created(data: Any): ResponseEntity<ApiResponse<Any>> =
        ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data))
}
