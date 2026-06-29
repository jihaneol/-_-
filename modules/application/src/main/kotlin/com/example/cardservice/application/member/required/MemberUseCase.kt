package com.example.cardservice.application.member.required

import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.member.CreateMemberInput
import com.example.cardservice.application.member.AuthenticatedMemberResult
import com.example.cardservice.application.member.LoginInput
import com.example.cardservice.application.member.MemberPageResult
import com.example.cardservice.application.member.MemberResult
import com.example.cardservice.application.member.UpdateMemberInput

/**
 * 회원 변경 요청을 application layer로 전달하는 inbound port다.
 */
interface MemberUseCase {
    fun createMember(input: CreateMemberInput): MemberResult
    fun updateMember(memberId: Long, input: UpdateMemberInput): MemberResult
    fun deleteMember(memberId: Long)
}

interface MemberAuthUseCase {
    fun authenticate(input: LoginInput): AuthenticatedMemberResult
}

/**
 * 회원 조회 요청을 application layer로 전달하는 inbound port다.
 */
interface MemberQueryUseCase {
    fun listMembers(pagination: Pagination): MemberPageResult
    fun getMember(memberId: Long): MemberResult
}
