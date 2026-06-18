package ca.uwaterloo.atlas.viewmodel

import ca.uwaterloo.atlas.data.repository.UserProfileRepository
import ca.uwaterloo.atlas.domain.profile.AgeRange
import ca.uwaterloo.atlas.domain.profile.Gender
import ca.uwaterloo.atlas.domain.profile.TravelStyleTag
import ca.uwaterloo.atlas.domain.profile.UserProfile
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.platform.getImageUploader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val savedProfile: UserProfile? = null,
    val draftProfile: UserProfile? = null,
    val publicTrips: List<TripData> = emptyList(),
    val savedTrips: List<TripData> = emptyList(),
    val isLoading: Boolean = true,
    val isEditing: Boolean = false
)

class ProfileViewModel(
    private val repo: UserProfileRepository,
    private val userId: String
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    fun loadProfile() {
        if (userId.isBlank()) {
            _uiState.value = ProfileUiState(isLoading = false)
        } else {
            // Only show full-screen loading if we have NO data yet
            if (_uiState.value.savedProfile == null) {
                _uiState.update { it.copy(isLoading = true) }
            }
            
            scope.launch {
                try {
                    val profile     = repo.getProfile(userId)
                    val publicTrips = repo.getMyPublicTrips(userId)
                    val savedTrips  = repo.getSavedTrips(userId)
                    
                    _uiState.update { s ->
                        s.copy(
                            savedProfile = profile,
                            // Don't overwrite draft if user is currently editing
                            draftProfile = if (s.isEditing) s.draftProfile else profile,
                            publicTrips  = publicTrips,
                            savedTrips   = savedTrips,
                            isLoading    = false
                        )
                    }
                } catch (e: Exception) {
                    println("[ProfileViewModel] load failed: ${e.message}")
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun refreshProfile() {
        loadProfile()
    }

    fun refreshSavedTrips() {
        if (userId.isBlank()) return
        scope.launch {
            try {
                val savedTrips = repo.getSavedTrips(userId)
                _uiState.update { it.copy(savedTrips = savedTrips) }
            } catch (e: Exception) {
                println("[ProfileViewModel] refreshSavedTrips failed: ${e.message}")
            }
        }
    }

    fun onEditClicked() {
        _uiState.update { s ->
            val saved = s.savedProfile ?: return@update s
            s.copy(isEditing = true, draftProfile = saved)
        }
    }

    fun onCancelClicked() {
        _uiState.update { s ->
            val saved = s.savedProfile ?: return@update s
            s.copy(isEditing = false, draftProfile = saved)
        }
    }

    fun onSaveClicked() {
        if (userId.isBlank()) return
        val draft = _uiState.value.draftProfile ?: return
        _uiState.update { s ->
            s.copy(isEditing = false, savedProfile = draft, draftProfile = draft)
        }
        scope.launch {
            try {
                repo.updateProfile(userId, draft)
            } catch (e: Exception) {
                println("[ProfileViewModel] updateProfile failed: ${e.message}")
            }
        }
    }

    fun onDisplayNameChange(newDisplayName: String) {
        _uiState.update { s ->
            if (!s.isEditing) return@update s
            val p = s.draftProfile ?: return@update s
            s.copy(draftProfile = p.copy(displayName = newDisplayName))
        }
    }

    fun onBioChange(newBio: String) {
        _uiState.update { s ->
            if (!s.isEditing) return@update s
            val p = s.draftProfile ?: return@update s
            s.copy(draftProfile = p.copy(bio = newBio))
        }
    }

    fun onGenderSelected(newGender: Gender) {
        _uiState.update { s ->
            if (!s.isEditing) return@update s
            val p = s.draftProfile ?: return@update s
            s.copy(draftProfile = p.copy(gender = newGender))
        }
    }

    fun onAgeRangeSelected(newAge: AgeRange) {
        _uiState.update { s ->
            if (!s.isEditing) return@update s
            val p = s.draftProfile ?: return@update s
            s.copy(draftProfile = p.copy(ageRange = newAge))
        }
    }

    fun onToggleTag(tag: TravelStyleTag) {
        _uiState.update { s ->
            if (!s.isEditing) return@update s
            val p = s.draftProfile ?: return@update s
            val newTags = if (p.tags.contains(tag)) p.tags - tag else p.tags + tag
            s.copy(draftProfile = p.copy(tags = newTags))
        }
    }

    fun onAvatarSelected(uri: String) {
        if (userId.isBlank()) return

        scope.launch {
            try {
                val finalUrl = if (uri.startsWith("content://")) {
                    val uploader = getImageUploader()
                    uploader.uploadImage(uri, "photos") ?: uri
                } else {
                    uri
                }

                _uiState.update { s ->
                    if (!s.isEditing) return@update s
                    val p = s.draftProfile ?: return@update s
                    s.copy(draftProfile = p.copy(avatarUrl = finalUrl))
                }

                println("[ProfileViewModel] avatar updated -> $finalUrl")
            } catch (e: Exception) {
                println("[ProfileViewModel] avatar upload failed: ${e.message}")
            }
        }
    }
}
