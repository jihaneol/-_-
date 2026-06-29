package com.example.cardservice.domain.member

import jakarta.persistence.Access
import jakarta.persistence.AccessType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Access(AccessType.FIELD)
@Table(name = "members")
class Member protected constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0L
        protected set

    @Column(name = "username", nullable = false, length = 80, unique = true)
    var username: String = ""
        protected set

    @Column(name = "password_hash", nullable = false, length = 200)
    var passwordHash: String = ""
        protected set

    @Column(name = "name", nullable = false, length = 100)
    var name: String = ""
        protected set

    @Column(name = "email", length = 200)
    var email: String? = null
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    var role: MemberRole = MemberRole.USER
        protected set

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
        protected set

    val deleted: Boolean
        get() = deletedAt != null

    private constructor(username: String, passwordHash: String, name: String?, email: String?, role: MemberRole) : this() {
        require(username.isNotBlank()) { "회원 아이디는 비어 있을 수 없습니다." }
        require(passwordHash.isNotBlank()) { "회원 비밀번호는 비어 있을 수 없습니다." }
        this.username = username
        this.passwordHash = passwordHash
        this.name = name.toNickname()
        this.email = email?.trim()?.takeIf { it.isNotBlank() }
        this.role = role
    }

    fun update(name: String?, email: String?) {
        require(!deleted) { "삭제된 회원은 수정할 수 없습니다." }
        this.name = name.toNickname()
        this.email = email?.trim()?.takeIf { it.isNotBlank() }
    }

    fun softDelete(now: LocalDateTime = LocalDateTime.now()) {
        if (deletedAt == null) {
            deletedAt = now
        }
    }

    companion object {
        fun create(username: String, passwordHash: String, name: String?, email: String?, role: MemberRole = MemberRole.USER): Member =
            Member(username.trim(), passwordHash, name, email, role)

        private fun String?.toNickname(): String =
            this?.trim()?.takeIf { it.isNotBlank() } ?: "member-${UUID.randomUUID().toString().take(8)}"
    }
}

enum class MemberRole {
    ADMIN,
    USER,
}
