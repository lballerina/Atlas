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

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loginError: Boolean = false,
    val loginSuccess: Boolean = false,
    val isLoading: Boolean = false,
    // Populated after a successful login — App.kt reads this to build
    // the repositories that need a real user ID.
    val userId: String? = null
)

class LoginViewModel(
    private val model: UserCredentialModel
) : ViewModel() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun tryLogin() {
        val email    = _uiState.value.email.trim()
        val password = _uiState.value.password
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(
                isLoading    = false,
                loginSuccess = false,
                loginError   = true
            )}
        }

        scope.launch {
            _uiState.update { it.copy(isLoading = true, loginError = false) }
            try {
                val success = model.login(email, password)
                if (success) {
                    // Fetch the real UUID (Supabase session UUID in remote mode,
                    // "mock_user_id" in mock mode) so App.kt can wire repositories.
                    val userId = model.getCurrentUserId()
                    print(userId)
                    _uiState.update { it.copy(
                        isLoading    = false,
                        loginSuccess = true,
                        loginError   = false,
                        userId       = userId
                    )}
                } else {
                    _uiState.update { it.copy(
                        isLoading    = false,
                        loginSuccess = false,
                        loginError   = true
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading    = false,
                    loginSuccess = false,
                    loginError   = true
                )}
            }
        }
    }

    fun reset() {
        _uiState.update { LoginUiState() }
    }
}
