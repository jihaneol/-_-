package com.example.cardservice.security

import com.example.cardservice.application.member.provided.MemberRepository
import com.example.cardservice.domain.member.MemberRole
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.util.Date
import javax.crypto.spec.SecretKeySpec
import io.jsonwebtoken.Jwts

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class ShopSecurityConfiguration {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun jwtTokenProvider(properties: JwtProperties): JwtTokenProvider = JwtTokenProvider(properties, Clock.systemUTC())

    @Bean
    fun jwtAuthenticationFilter(jwtTokenProvider: JwtTokenProvider, memberRepository: MemberRepository): JwtAuthenticationFilter =
        JwtAuthenticationFilter(jwtTokenProvider, memberRepository)

    @Bean
    fun securityFilterChain(http: HttpSecurity, jwtAuthenticationFilter: JwtAuthenticationFilter): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/actuator/health",
                    "/api/shop/auth/login",
                    "/api/shop/auth/signup",
                    "/api/shop/members",
                    "/api/shop/products",
                    "/api/shop/products/**",
                ).permitAll()
                it.requestMatchers("/api/shop/**").hasRole(MemberRole.USER.name)
                it.anyRequest().permitAll()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
}

@ConfigurationProperties(prefix = "security.jwt")
data class JwtProperties(
    val secret: String = "card-service-local-development-secret-key-32",
    val expirationMinutes: Long = 60,
)

class JwtTokenProvider(
    private val properties: JwtProperties,
    private val clock: Clock,
) {
    private val key = SecretKeySpec(properties.secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")

    fun createToken(username: String, role: MemberRole): String {
        val now = clock.instant()
        return Jwts.builder()
            .subject(username)
            .claim("role", role.name)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(properties.expirationMinutes * 60)))
            .signWith(key)
            .compact()
    }

    fun parse(token: String): JwtPrincipal {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
        return JwtPrincipal(
            username = claims.subject,
            role = MemberRole.valueOf(claims["role"] as String),
        )
    }
}

data class JwtPrincipal(val username: String, val role: MemberRole)

class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val memberRepository: MemberRepository,
) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val token = request.getHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith("Bearer ") }
            ?.removePrefix("Bearer ")

        if (!token.isNullOrBlank() && SecurityContextHolder.getContext().authentication == null) {
            runCatching {
                val principal = jwtTokenProvider.parse(token)
                val member = memberRepository.findByUsernameAndDeletedAtIsNull(principal.username)
                if (member != null && member.role == principal.role) {
                    val authority = SimpleGrantedAuthority("ROLE_${member.role.name}")
                    SecurityContextHolder.getContext().authentication =
                        UsernamePasswordAuthenticationToken(principal.username, null, listOf(authority))
                }
            }
        }

        filterChain.doFilter(request, response)
    }
}
