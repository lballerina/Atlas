package ca.uwaterloo.atlas.viewmodel

import androidx.lifecycle.ViewModel
import ca.uwaterloo.atlas.data.repository.OtherUserProfileRepository
import ca.uwaterloo.atlas.domain.profile.UserProfile
import ca.uwaterloo.atlas.domain.trip.TripData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OtherProfileUiState(
    val profile: UserProfile? = null,
    val publicTrips: List<TripData> = emptyList(),
    val isLoading: Boolean = true
)

class OtherProfileViewModel(
    private val repo: OtherUserProfileRepository
) : ViewModel() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(OtherProfileUiState())
    val uiState: StateFlow<OtherProfileUiState> = _uiState

    fun load(authorName: String) {
        if (authorName.isBlank()) return
        
        // Only show full-screen loading if we are switching users or have no data
        if (_uiState.value.profile?.displayName != authorName) {
            _uiState.update { it.copy(isLoading = true, profile = null, publicTrips = emptyList()) }
        }

        scope.launch {
            try {
                val profile = repo.getProfile(authorName)
                val trips   = repo.getPublicTrips(authorName)
                println("[OtherProfile] loaded authorName=$authorName → ${trips.size} trips")
                _uiState.update { s ->
                    s.copy(
                        profile     = profile,
                        publicTrips = trips,
                        isLoading   = false
                    )
                }
            } catch (e: Exception) {
                println("[OtherProfile] load FAILED for $authorName: ${e::class.simpleName}: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
