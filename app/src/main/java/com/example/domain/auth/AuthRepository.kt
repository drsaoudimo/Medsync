package com.example.domain.auth

import com.example.data.auth.AuthResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(email: String, pass: String): Flow<AuthResult<Unit>>
    fun register(email: String, pass: String): Flow<AuthResult<Unit>>
    fun logout()
    fun currentUser(): String?
}
