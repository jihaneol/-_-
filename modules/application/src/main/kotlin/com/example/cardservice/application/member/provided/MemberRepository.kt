package com.example.cardservice.application.member.provided

import com.example.cardservice.domain.member.Member
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.Repository

/**
 * Member entity 저장과 기본 조회를 위해 application layer가 정의하는 Spring Data repository 계약이다.
 */
interface MemberRepository : Repository<Member, Long> {
    fun save(member: Member): Member
    fun findAllByDeletedAtIsNull(pageable: Pageable): Page<Member>
    fun findByIdAndDeletedAtIsNull(id: Long): Member?
    fun findByUsernameAndDeletedAtIsNull(username: String): Member?
    fun existsByUsername(username: String): Boolean
}
