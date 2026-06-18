package ca.uwaterloo.atlas.data.repository

import ca.uwaterloo.atlas.data.MockDB
import ca.uwaterloo.atlas.domain.credential.UserCredential

/**
 * Mock (in-memory) implementation of [UserCredentialRepository].
 *
 * Tracks which user is currently "logged in" so [getCurrentUserId] returns
 * a stable, per-user identifier rather than a single constant. This means
 * [TripModel] and [ProfileViewModel] rebuild correctly when a different mock
 * user signs in or signs up within the same session.
 *
 * User ID derivation: we use the email address itself as the mock UUID.
 * It is stable (same email always produces the same ID), unique per user,
 * and requires no real UUID generation.
 *
 * Only use in unit tests — swap to [RemoteUserCredentialRepository] in App.kt.
 */
class MockUserCredentialRepository(
    initialUsers: List<UserCredential> = MockDB.allUsers
) : UserCredentialRepository {

    private val users = initialUsers.toMutableList()

    // Tracks the currently authenticated user's email; null = not logged in
    private var loggedInEmail: String? = null

    override suspend fun login(email: String, password: String): Boolean {
        val success = users.any { it.email == email && it.password == password }
        if (success) loggedInEmail = email
        return success
    }

    override suspend fun signUp(email: String, password: String): UserCredential {
        check(!emailExists(email)) { "Email $email is already registered" }
        val credential = UserCredential(email = email, password = password)
        users.add(credential)
        // Immediately log the new user in
        loggedInEmail = email
        return credential
    }

    override suspend fun emailExists(email: String): Boolean =
        users.any { it.email == email }

    /**
     * Returns a deterministic mock UUID derived from the logged-in email.
     * The "mock_user_id" fallback only applies if no one has logged in yet,
     * which should never happen in normal app flow since login/signup always
     * calls this after a successful auth.
     */
    override suspend fun getCurrentUserId(): String =
        loggedInEmail ?: "mock_user_id"
}
