package ca.uwaterloo.atlas.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.atlas.domain.place.CostLevel
import ca.uwaterloo.atlas.domain.place.Place
import ca.uwaterloo.atlas.domain.place.PlaceModel
import ca.uwaterloo.atlas.domain.place.PlaceCategory
import ca.uwaterloo.atlas.domain.place.TimeOfDay
import ca.uwaterloo.atlas.domain.trip.TripAccessMode
import ca.uwaterloo.atlas.platform.getImageUploader
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * ViewModel for the Single Pin screen.
 *
 * Responsibilities:
 *  - Delegates all domain logic to PlaceModel
 *  - Exposes Place state to UI
 *  - Manages UI-only state (edit mode, editing buffer)
 */
class SinglePinViewModel(
    private val placeModel: PlaceModel,
    private val accessMode: TripAccessMode
) : ViewModel() {
    private companion object {
        const val MAX_PIN_PHOTOS = 10
    }

    // ── Domain state ──────────────────────────────────────────────────────

    var place by mutableStateOf<Place?>(null)
        private set

    val isEditable: Boolean
        get() = accessMode == TripAccessMode.EDIT

    // ── Editable metadata fields ──────────────────────────────────────────

    var dateVisited    by mutableStateOf<LocalDate?>(null)
        private set
    var rating         by mutableStateOf<Float?>(null)
    var price          by mutableStateOf<CostLevel?>(null)
    var selectedCategory by mutableStateOf(PlaceCategory.CAFE)
    var timeOfDay      by mutableStateOf<TimeOfDay?>(null)
    var tags           by mutableStateOf(emptyList<String>())
    var selectedMood   by mutableStateOf<String?>(null)
    var costAmount     by mutableStateOf<Double?>(null)
    var newTag         by mutableStateOf("")
    var isFavorite     by mutableStateOf(false)
    var timeExpanded   by mutableStateOf(false)
    var metadataExpanded by mutableStateOf(false)
    var oldPhotos         by mutableStateOf<List<String>>(emptyList())
    var oldThumbnail by mutableStateOf<String?>(null)
    var oldCaption by mutableStateOf<Map<String, String>>(emptyMap())

    // ── UI-only state ─────────────────────────────────────────────────────

    var isEditing by mutableStateOf(false)
        private set

    var editingText by mutableStateOf("")
        private set

    // ── Load ──────────────────────────────────────────────────────────────

    fun loadPlace(placeId: String) {
        viewModelScope.launch {
            val result = placeModel.getPlaceById(placeId)
            place            = result
            rating           = result?.rating
            price            = result?.costIndicator
            selectedMood     = result?.mood
            dateVisited      = result?.dateVisited
            selectedCategory = result?.category     ?: PlaceCategory.CAFE
            timeOfDay        = result?.timeOfDay
            isFavorite       = result?.isFavorite   ?: false
            tags             = result?.tags         ?: emptyList()
            costAmount       = result?.costAmount
            oldPhotos        = result?.photos       ?: emptyList()
            oldThumbnail     = result?.thumbnailPhoto
            oldCaption       = result?.photoCaptions    ?: emptyMap()
        }
    }

    // ── UI state helpers ──────────────────────────────────────────────────

    fun startEditing(currentNotes: String) {
        editingText = currentNotes
        isEditing = true
    }

    fun updateEditingText(newText: String) { editingText = newText }
    fun updateDateVisited(newDate: LocalDate?) { dateVisited = newDate }
    fun updateRating(newRating: Float?) { rating = newRating }
    fun updateCategory(newCategory: PlaceCategory) { selectedCategory = newCategory }
    fun updateTimeOfDay(newTime: TimeOfDay?) { timeOfDay = newTime }
    fun updateFavorite(newFav: Boolean) { isFavorite = newFav }
    fun updateTags(newTags: List<String>) { tags = newTags }
    fun updateMood(newMood: String?) { selectedMood = newMood }
    fun updateCostAmount(newAmount: Double?) { costAmount = newAmount }
    fun updatePrice(newPrice: Int?) {
        price = when (newPrice) {
            1    -> CostLevel.BUDGET
            2    -> CostLevel.MODERATE
            3    -> CostLevel.EXPENSIVE
            4    -> CostLevel.LUXURY
            else -> null
        }
    }

    fun cancelEditing() {
        metadataExpanded = false
        editingText      = place?.notes         ?: ""
        rating           = place?.rating
        price            = place?.costIndicator
        selectedCategory = place?.category      ?: PlaceCategory.CAFE
        timeOfDay        = place?.timeOfDay
        isFavorite       = place?.isFavorite    ?: false
        tags             = place?.tags          ?: emptyList()
        selectedMood     = place?.mood
        costAmount       = place?.costAmount
        dateVisited      = place?.dateVisited
        isEditing        = false
        // revert photos back to old version!
        updatePhotos(oldPhotos, oldThumbnail, oldCaption)
    }

    fun finishEditing() {
        metadataExpanded = false
        isEditing = false
    }

    fun exit() { cancelEditing() }

    // ── Domain mutations (suspend — wrapped in coroutines) ────────────────

    fun updateNotes(newNotes: String) {
        if (!isEditable) return
        val current = place ?: return
        val updated = current.copy(notes = newNotes)
        place = updated
        viewModelScope.launch {
            placeModel.updatePlace(updated)
        }
    }

    fun updateMetadata(
        newDate: LocalDate?,
        newPrice: CostLevel?,
        newRating: Float?,
        newCategory: PlaceCategory,
        newTags: List<String>,
        newTime: TimeOfDay?,
        newFav: Boolean,
        newMood: String?,
        newAmount: Double?
    ) {
        if (!isEditable) return
        val current = place ?: return
        val updated = current.copy(
            dateVisited   = newDate,
            costIndicator = newPrice,
            rating        = newRating,
            category      = newCategory,
            tags          = newTags,
            timeOfDay     = newTime,
            isFavorite    = newFav,
            mood          = newMood,
            costAmount    = newAmount
        )
        place = updated
        viewModelScope.launch {
            placeModel.updatePlace(updated)
        }
    }

    fun updatePhotos(
        photos: List<String>,
        thumbnail: String?,
        captions: Map<String, String>
    ) {
        if (!isEditable) return
        val current = place ?: return

        val limitedPhotos = photos.take(MAX_PIN_PHOTOS)
        println("[SinglePinViewModel] updatePhotos called with ${photos.size} photos (capped to ${limitedPhotos.size})")
        viewModelScope.launch {
            val uploader = getImageUploader()
            val uploadedPhotos = limitedPhotos.map { uri ->
                if (uri.startsWith("content://")) {
                    println("[SinglePinViewModel] Local URI detected, uploading: $uri")
                    val result = uploader.uploadImage(uri, "photos")
                    println("[SinglePinViewModel] Upload result for $uri -> $result")
                    result ?: uri
                } else {
                    uri
                }
            }

            println("[SinglePinViewModel] Uploaded photos: $uploadedPhotos")

            val updatedThumbnail = when {
                thumbnail == null -> uploadedPhotos.firstOrNull()
                thumbnail.startsWith("content://") -> {
                    val index = limitedPhotos.indexOf(thumbnail)
                    if (index != -1) uploadedPhotos[index] else uploadedPhotos.firstOrNull()
                }
                thumbnail in uploadedPhotos -> thumbnail
                else -> uploadedPhotos.firstOrNull()
            }

            // Keep captions aligned with the capped photo set.
            val updatedCaptions = limitedPhotos.mapIndexedNotNull { index, uri ->
                val newUri = uploadedPhotos.getOrNull(index) ?: return@mapIndexedNotNull null
                val caption = captions[uri] ?: captions[newUri] ?: return@mapIndexedNotNull null
                newUri to caption
            }.toMap()

            val updated = current.copy(
                photos         = uploadedPhotos,
                thumbnailPhoto = updatedThumbnail,
                photoCaptions  = updatedCaptions
            )
            println("[SinglePinViewModel] Saving updated place with ${updated.photos.size} photos")
            place = updated
            placeModel.updatePlace(updated)
        }
    }

    fun deletePlace() {
        if (!isEditable) return
        val current = place ?: return
        viewModelScope.launch {
            placeModel.deletePlace(current.id)
            place = null
        }
    }
}
