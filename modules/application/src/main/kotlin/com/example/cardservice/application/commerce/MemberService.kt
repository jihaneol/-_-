package com.example.cardservice.application.commerce

import com.example.cardservice.application.commerce.provided.MemberRepository
import com.example.cardservice.application.commerce.required.MemberUseCase
import com.example.cardservice.domain.commerce.model.member.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 회원 생성, 수정, 삭제 흐름을 조율하는 application service다.
 */
@Service
class MemberService(
    private val memberRepository: MemberRepository,
) : MemberUseCase {
    @Transactional
    override fun createMember(input: CreateMemberInput): MemberResult =
        memberRepository.save(Member.create(input.name, input.email)).toResult()

    @Transactional
    override fun updateMember(memberId: Long, input: UpdateMemberInput): MemberResult {
        val member = loadMember(memberId)
        member.update(input.name, input.email)
        return memberRepository.save(member).toResult()
    }

    @Transactional
    override fun deleteMember(memberId: Long) {
        val member = loadMember(memberId)
        member.softDelete()
        memberRepository.save(member)
    }

    private fun loadMember(memberId: Long): Member =
        memberRepository.findByIdAndDeletedAtIsNull(memberId) ?: throw IllegalArgumentException("회원을 찾을 수 없습니다.")
}

internal fun Member.toResult(): MemberResult =
    MemberResult(id = requireNotNull(id), name = name, email = email)
