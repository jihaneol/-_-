package com.example.cardservice.web.member

import com.example.cardservice.application.common.DEFAULT_PAGE_SIZE
import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.member.CreateMemberInput
import com.example.cardservice.application.member.MemberPageResult
import com.example.cardservice.application.member.MemberResult
import com.example.cardservice.application.member.UpdateMemberInput
import com.example.cardservice.application.member.request.MemberRequest
import com.example.cardservice.application.member.required.MemberQueryUseCase
import com.example.cardservice.application.member.required.MemberUseCase
import com.example.cardservice.web.common.ApplicationResponseType
import com.example.cardservice.web.common.ApiResponse
import com.example.cardservice.web.common.toApplicationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/members")
@Tag(name = "Member", description = "회원 운영 API")
class MemberController(
    private val memberUseCase: MemberUseCase,
    private val memberQueryUseCase: MemberQueryUseCase,
) {
    @PostMapping
    @Operation(summary = "회원 생성")
    fun createMember(@RequestBody request: MemberRequest): ResponseEntity<ApiResponse<MemberResult>> =
        memberUseCase
            .createMember(CreateMemberInput(request.name, request.email))
            .toApplicationResponse(ApplicationResponseType.CREATED)

    @GetMapping
    @Operation(summary = "회원 목록 조회")
    fun listMembers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "$DEFAULT_PAGE_SIZE") size: Int,
        @RequestParam(defaultValue = "id,desc") sort: String,
    ): ResponseEntity<ApiResponse<MemberPageResult>> =
        memberQueryUseCase.listMembers(Pagination(page, size, sort)).toApplicationResponse()

    @GetMapping("/{memberId}")
    @Operation(summary = "회원 상세 조회")
    fun getMember(@PathVariable memberId: Long): ResponseEntity<ApiResponse<MemberResult>> =
        memberQueryUseCase.getMember(memberId).toApplicationResponse()

    @PatchMapping("/{memberId}")
    @Operation(summary = "회원 수정")
    fun updateMember(
        @PathVariable memberId: Long,
        @RequestBody request: MemberRequest,
    ): ResponseEntity<ApiResponse<MemberResult>> =
        memberUseCase
            .updateMember(memberId, UpdateMemberInput(request.name, request.email))
            .toApplicationResponse()

    @DeleteMapping("/{memberId}")
    @Operation(summary = "회원 소프트 삭제")
    fun deleteMember(@PathVariable memberId: Long): ResponseEntity<ApiResponse<Unit>> {
        memberUseCase.deleteMember(memberId)
        return Unit.toApplicationResponse(ApplicationResponseType.NO_CONTENT)
    }
}
