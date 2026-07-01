package com.example.cardservice.application.member.required

import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.member.CreateMemberRequest
import com.example.cardservice.application.member.AuthenticatedMemberResponse
import com.example.cardservice.application.member.LoginRequest
import com.example.cardservice.application.member.MemberPageResponse
import com.example.cardservice.application.member.MemberResponse
import com.example.cardservice.application.member.UpdateMemberRequest

/**
 * 회원 변경 요청을 application layer로 전달하는 inbound port다.
 */
interface MemberUseCase {
    fun createMember(input: CreateMemberRequest): MemberResponse
    fun updateMember(request: UpdateMemberRequest): MemberResponse
    fun deleteMember(memberId: Long)
}

interface MemberAuthUseCase {
    fun authenticate(input: LoginRequest): AuthenticatedMemberResponse
}

/**
 * 회원 조회 요청을 application layer로 전달하는 inbound port다.
 */
interface MemberQueryUseCase {
    fun listMembers(pagination: Pagination): MemberPageResponse
    fun getMember(memberId: Long): MemberResponse
}
