package ca.uwaterloo.atlas.domain.credential

import ca.uwaterloo.atlas.data.repository.UserCredentialRepository

/**
 * Domain Model for user credentials.
 *
 * Delegates all persistence to [UserCredentialRepository] so the domain
 * layer never imports MockDB or Supabase directly.
 *
 * ViewModels (LoginViewModel, SignupViewModel) hold an instance of this.
 */
class UserCredentialModel(
    val repository: UserCredentialRepository
) {
    suspend fun login(email: String, password: String): Boolean =
        repository.login(email, password)

    suspend fun signUp(email: String, password: String): UserCredential =
        repository.signUp(email, password)

    suspend fun emailExists(email: String): Boolean =
        repository.emailExists(email)

    suspend fun getCurrentUserId(): String? =
        repository.getCurrentUserId()
}
