package com.example.cardservice.application.commerce.required

import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.commerce.CreateMemberInput
import com.example.cardservice.application.commerce.MemberPageResult
import com.example.cardservice.application.commerce.MemberResult
import com.example.cardservice.application.commerce.UpdateMemberInput

/**
 * 회원 변경 요청을 application layer로 전달하는 inbound port다.
 */
interface MemberUseCase {
    fun createMember(input: CreateMemberInput): MemberResult
    fun updateMember(memberId: Long, input: UpdateMemberInput): MemberResult
    fun deleteMember(memberId: Long)
}

/**
 * 회원 조회 요청을 application layer로 전달하는 inbound port다.
 */
interface MemberQueryUseCase {
    fun listMembers(pagination: Pagination): MemberPageResult
    fun getMember(memberId: Long): MemberResult
}
