package com.example.cardservice.application.member

import com.example.cardservice.application.member.provided.MemberRepository
import com.example.cardservice.application.member.provided.PasswordHashPort
import com.example.cardservice.application.member.required.MemberAuthUseCase
import com.example.cardservice.application.member.required.MemberUseCase
import com.example.cardservice.domain.member.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 회원 생성, 수정, 삭제 흐름을 조율하는 application service다.
 */
@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordHashPort: PasswordHashPort,
) : MemberUseCase {
    @Transactional
    override fun createMember(input: CreateMemberInput): MemberResult {
        require(input.username.isNotBlank()) { "회원 아이디는 비어 있을 수 없습니다." }
        require(input.password.isNotBlank()) { "회원 비밀번호는 비어 있을 수 없습니다." }
        require(!memberRepository.existsByUsername(input.username.trim())) { "이미 사용 중인 회원 아이디입니다." }

        return memberRepository
            .save(
                Member.create(
                    username = input.username,
                    passwordHash = passwordHashPort.encode(input.password),
                    name = input.name,
                    email = input.email,
                    role = input.role,
                ),
            )
            .toResult()
    }

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

@Service
class MemberAuthenticationService(
    private val memberRepository: MemberRepository,
    private val passwordHashPort: PasswordHashPort,
) : MemberAuthUseCase {
    override fun authenticate(input: LoginInput): AuthenticatedMemberResult {
        val member = memberRepository.findByUsernameAndDeletedAtIsNull(input.username.trim())
            ?: throw IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.")

        require(passwordHashPort.matches(input.password, member.passwordHash)) {
            "아이디 또는 비밀번호가 올바르지 않습니다."
        }

        return AuthenticatedMemberResult(
            id = member.id,
            username = member.username,
            name = member.name,
            email = member.email,
            role = member.role,
        )
    }
}

internal fun Member.toResult(): MemberResult =
    MemberResult(id = id, username = username, name = name, email = email, role = role)
