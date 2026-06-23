package com.example.cardservice.domain.commerce.model.member

import jakarta.persistence.Access
import jakarta.persistence.AccessType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Access(AccessType.FIELD)
@Table(name = "members")
class Member protected constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
        protected set

    @Column(name = "name", nullable = false, length = 100)
    var name: String = ""
        protected set

    @Column(name = "email", nullable = false, length = 200)
    var email: String = ""
        protected set

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
        protected set

    val deleted: Boolean
        get() = deletedAt != null

    private constructor(name: String, email: String) : this() {
        require(name.isNotBlank()) { "회원 이름은 비어 있을 수 없습니다." }
        require(email.isNotBlank()) { "회원 이메일은 비어 있을 수 없습니다." }
        this.name = name
        this.email = email
    }

    fun update(name: String, email: String) {
        require(!deleted) { "삭제된 회원은 수정할 수 없습니다." }
        require(name.isNotBlank()) { "회원 이름은 비어 있을 수 없습니다." }
        require(email.isNotBlank()) { "회원 이메일은 비어 있을 수 없습니다." }
        this.name = name
        this.email = email
    }

    fun softDelete(now: LocalDateTime = LocalDateTime.now()) {
        if (deletedAt == null) {
            deletedAt = now
        }
    }

    companion object {
        fun create(name: String, email: String): Member = Member(name, email)
    }
}
