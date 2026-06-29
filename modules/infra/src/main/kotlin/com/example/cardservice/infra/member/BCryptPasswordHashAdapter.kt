package com.example.cardservice.infra.member

import com.example.cardservice.application.member.provided.PasswordHashPort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptPasswordHashAdapter(
    private val passwordEncoder: PasswordEncoder,
) : PasswordHashPort {
    override fun encode(rawPassword: String): String = passwordEncoder.encode(rawPassword)

    override fun matches(rawPassword: String, encodedPassword: String): Boolean =
        passwordEncoder.matches(rawPassword, encodedPassword)
}
