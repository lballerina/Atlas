package ca.uwaterloo.atlas.data.repository

import ca.uwaterloo.atlas.domain.credential.UserCredential

/**
 * DB Interface for user credential persistence (sign-up / sign-in / existence check).
 */
interface UserCredentialRepository {
    /** Returns true if credentials match a known user. */
    suspend fun login(email: String, password: String): Boolean

    /**
     * Creates a new account.
     * Returns the created [UserCredential] on success.
     * Throws [IllegalStateException] if the email is already registered.
     */
    suspend fun signUp(email: String, password: String): UserCredential

    /** Returns true if an account with [email] already exists. */
    suspend fun emailExists(email: String): Boolean

    /**
     * Returns the Supabase UUID for the currently authenticated session,
     * or null if not signed in. Used to pass [currentUserId] to repositories.
     */
    suspend fun getCurrentUserId(): String?
}
