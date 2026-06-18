package ca.uwaterloo.atlas.data.repository

import ca.uwaterloo.atlas.SupabaseClient
import ca.uwaterloo.atlas.data.dto.UserInsertDto
import ca.uwaterloo.atlas.domain.credential.UserCredential
import ca.uwaterloo.atlas.domain.profile.AgeRange
import ca.uwaterloo.atlas.domain.profile.Gender
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

class RemoteUserCredentialRepository : UserCredentialRepository {

    private val auth get() = SupabaseClient.client.auth
    private val db   get() = SupabaseClient.client.postgrest

    override suspend fun login(email: String, password: String): Boolean {
        return try {
            auth.signInWith(Email) {
                this.email    = email
                this.password = password
            }
            true
        } catch (e: Exception) {
            println("[Auth] login failed — ${e::class.simpleName}: ${e.message}")
            false
        }
    }

    override suspend fun signUp(email: String, password: String): UserCredential {
        try {
            auth.signUpWith(Email) {
                this.email    = email
                this.password = password
            }
        } catch (e: Exception) {
            val msg = e.message ?: ""
            println("[Auth] signUpWith failed — ${e::class.simpleName}: $msg")
            if (msg.contains("already registered", ignoreCase = true) ||
                msg.contains("User already registered", ignoreCase = true) ||
                msg.contains("email address is already", ignoreCase = true) ||
                msg.contains("already been registered", ignoreCase = true)
            ) {
                throw IllegalStateException("Email $email is already registered")
            }
            throw e
        }

        val userId = getCurrentUserId()
        if (userId == null) {
            println("[Auth] signUp succeeded but no session — is email confirmation enabled?")
            return UserCredential(email = email, password = "")
        }

        try {
            db["users"].insert(
                UserInsertDto(
                    id              = userId,
                    avatarUrl       = "",
                    displayName     = email.substringBefore("@"),
                    email           = email,
                    bio             = "",
                    gender          = Gender.PREFER_NOT_TO_SAY.name,
                    ageRange        = AgeRange.LATE_20S.name,
                    travelStyleTags = emptyList(),
                    tripsCount      = 0,
                    placesCount     = 0,
                    countriesCount  = 0
                )
            )
            println("[Auth] users row created for $userId")
        } catch (e: Exception) {
            println("[Auth] users row insert failed — ${e::class.simpleName}: ${e.message}")
        }

        return UserCredential(email = email, password = "")
    }

    override suspend fun emailExists(email: String): Boolean {
        return try {
            auth.signInWith(Email) {
                this.email    = email
                this.password = "___sentinel___"
            }
            true
        } catch (e: Exception) {
            val msg = e.message ?: ""
            msg.contains("Invalid login credentials", ignoreCase = true)
        }
    }

    override suspend fun getCurrentUserId(): String? =
        auth.currentUserOrNull()?.id
}
