package com.example.data.auth

import com.example.domain.auth.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AuthRepositoryImpl(
    private val auth: FirebaseAuth
) : AuthRepository {
    override fun login(email: String, pass: String): Flow<AuthResult<Unit>> = callbackFlow {
        trySend(AuthResult.Loading)
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(AuthResult.Success(Unit))
                } else {
                    trySend(AuthResult.Error(task.exception?.message ?: "Unknown Error"))
                }
                close()
            }
        awaitClose { }
    }

    override fun register(email: String, pass: String): Flow<AuthResult<Unit>> = callbackFlow {
        trySend(AuthResult.Loading)
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(AuthResult.Success(Unit))
                } else {
                    trySend(AuthResult.Error(task.exception?.message ?: "Unknown Error"))
                }
                close()
            }
        awaitClose { }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun currentUser(): String? = auth.currentUser?.email
}
