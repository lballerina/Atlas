package ca.uwaterloo.atlas.viewmodel

import androidx.lifecycle.ViewModel
import ca.uwaterloo.atlas.domain.credential.UserCredentialModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignupUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val signupError: Boolean = false,
    val signupSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String = "Account already exists.",
    // Populated after a successful sign-up — App.kt reads this to build
    // the repositories that need a real user ID.
    val userId: String? = null
)

class SignupViewModel(
    private val model: UserCredentialModel
) : ViewModel() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState

    fun onFirstNameChange(name: String) {
        _uiState.update { it.copy(firstName = name) }
    }

    fun onLastNameChange(name: String) {
        _uiState.update { it.copy(lastName = name) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun trySignup() {
        val firstName = _uiState.value.firstName.trim()
        val lastName  = _uiState.value.lastName.trim()
        val email     = _uiState.value.email.trim()
        val password  = _uiState.value.password
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(
                isLoading     = false,
                signupSuccess = false,
                signupError   = true,
                errorMessage  = "All fields must be filled."
            )}
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(
                isLoading     = false,
                signupSuccess = false,
                signupError   = true,
                errorMessage  = "Password must be at least 6 characters long."
            )}
            return
        }

        scope.launch {
            _uiState.update { it.copy(isLoading = true, signupError = false) }
            try {
                // Delegate entirely to the repository — no more manual list iteration
                val credential = model.signUp(
                    email    = email,
                    password = password
                )

                // Fetch the real UUID (Supabase session UUID in remote mode,
                // "mock_user_id" in mock mode) so App.kt can wire repositories.
                val userId = model.getCurrentUserId()

                _uiState.update { it.copy(
                    isLoading      = false,
                    signupSuccess  = true,
                    signupError    = false,
                    userId         = userId
                )}

            } catch (e: IllegalStateException) {
                // signUp throws IllegalStateException when email already exists
                _uiState.update { it.copy(
                    isLoading     = false,
                    signupSuccess = false,
                    signupError   = true,
                    errorMessage  = "Account already exists."
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading     = false,
                    signupSuccess = false,
                    signupError   = true,
                    errorMessage  = "Sign up failed. Please try again."
                )}
            }
        }
    }

    fun reset() {
        _uiState.update { SignupUiState() }
    }
}
