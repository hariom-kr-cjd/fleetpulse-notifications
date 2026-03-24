package com.fleetpulse.notifications

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

const val TEST_JWT_SECRET = "default-secret"
const val TEST_INTERNAL_KEY = "default-key"

fun generateTestJwt(userId: String, role: String = "driver"): String {
    return JWT.create()
        .withClaim("userId", userId)
        .withClaim("role", role)
        .withExpiresAt(Date(System.currentTimeMillis() + 15 * 60 * 1000))
        .sign(Algorithm.HMAC256(TEST_JWT_SECRET))
}
