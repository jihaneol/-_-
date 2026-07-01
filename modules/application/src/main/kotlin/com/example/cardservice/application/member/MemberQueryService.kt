package com.example.cardservice.application.member

import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.common.toPageable
import com.example.cardservice.application.member.provided.MemberRepository
import com.example.cardservice.application.member.required.MemberQueryUseCase
import com.example.cardservice.domain.member.Member
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 회원 조회 흐름을 조율하는 application query service다.
 */
@Service
class MemberQueryService(
    private val memberRepository: MemberRepository,
) : MemberQueryUseCase {
    @Transactional(readOnly = true)
    override fun listMembers(pagination: Pagination): MemberPageResponse =
        memberRepository.findAllByDeletedAtIsNull(pagination.toPageable()).toPageResponse()

    @Transactional(readOnly = true)
    override fun getMember(memberId: Long): MemberResponse =
        (memberRepository.findByIdAndDeletedAtIsNull(memberId) ?: throw IllegalArgumentException("회원을 찾을 수 없습니다."))
            .toResponse()

    private fun Page<Member>.toPageResponse(): MemberPageResponse =
        MemberPageResponse(
            items = content.map { it.toResponse() },
            page = number,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
            hasNext = hasNext(),
        )
}
