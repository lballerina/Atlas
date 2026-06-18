package ca.uwaterloo.atlas.viewmodel

import ca.uwaterloo.atlas.data.repository.UserProfileRepository
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.domain.profile.AgeRange
import ca.uwaterloo.atlas.domain.profile.Gender
import ca.uwaterloo.atlas.domain.profile.TravelStats
import ca.uwaterloo.atlas.domain.profile.TravelStyleTag
import ca.uwaterloo.atlas.domain.profile.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private fun runVmTest(block: suspend TestScope.() -> Unit) =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(dispatcher)
            try {
                block()
            } finally {
                Dispatchers.resetMain()
            }
        }
    private class TestProfileRepository(
        private var storedProfile: UserProfile,
        private var publicTrips: List<TripData>,
        private var savedTrips: List<TripData>
    ) : UserProfileRepository {

        var updateCalls = 0
            private set
        var lastUpdated: UserProfile? = null
            private set
        var getSavedTripsCalls = 0
            private set

        override suspend fun getProfile(userId: String): UserProfile = storedProfile

        override suspend fun updateProfile(userId: String, profile: UserProfile) {
            updateCalls++
            lastUpdated = profile
            storedProfile = profile
        }

        override suspend fun getMyPublicTrips(userId: String): List<TripData> = publicTrips

        override suspend fun getSavedTrips(userId: String): List<TripData> {
            getSavedTripsCalls++
            return savedTrips
        }

        fun updateSavedTrips(newTrips: List<TripData>) {
            savedTrips = newTrips
        }
    }

    private fun sampleProfile(
        displayName: String = "Andrew Anderson",
        bio: String = "Hello world",
        gender: Gender = Gender.MALE,
        ageRange: AgeRange = AgeRange.LATE_20S,
        tags: Set<TravelStyleTag> = setOf(TravelStyleTag.SOLO)
    ) = UserProfile(
        displayName = displayName,
        avatarUrl = null,
        email = "andrew.anderson@email.com",
        bio = bio,
        gender = gender,
        ageRange = ageRange,
        tags = tags,
        stats = TravelStats(trips = 12, places = 47, countries = 8)
    )

    private fun sampleTrips(): Pair<List<TripData>, List<TripData>> {
        val public = listOf(
            TripData(
                id = "pub-1",
                title = "Public Trip 1",
                location = "Tokyo",
                imageUrl = "",
                isPublic = true,
                author = "Andrew Anderson"
            ),
            TripData(
                id = "pub-2",
                title = "Public Trip 2",
                location = "Paris",
                imageUrl = "",
                isPublic = true,
                author = "Andrew Anderson"
            )
        )

        val saved = listOf(
            TripData(
                id = "saved-1",
                title = "Saved Trip 1",
                location = "Taipei",
                imageUrl = "",
                isPublic = true,
                author = "Sofia"
            )
        )

        return public to saved
    }

    @Test
    fun `init loads profile and trips and sets isLoading false`() = runVmTest {
        val (pub, saved) = sampleTrips()
        val repo = TestProfileRepository(
            storedProfile = sampleProfile(),
            publicTrips = pub,
            savedTrips = saved
        )

        val vm = ProfileViewModel(repo, "test-user-id")
        advanceUntilIdle()

        val s = vm.uiState.value
        assertFalse(s.isLoading)
        assertFalse(s.isEditing)
        assertNotNull(s.savedProfile)
        assertNotNull(s.draftProfile)
        assertEquals("Andrew Anderson", s.savedProfile.displayName)
        assertEquals(2, s.publicTrips.size)
        assertEquals(1, s.savedTrips.size)
        assertEquals("pub-1", s.publicTrips.first().id)
        assertEquals("saved-1", s.savedTrips.first().id)
    }

    // ─────────────────────────────────────────────
    // Edit / cancel
    // ─────────────────────────────────────────────

    @Test
    fun `onEditClicked enters edit mode`() = runVmTest {
        val (pub, saved) = sampleTrips()
        val repo = TestProfileRepository(sampleProfile(), pub, saved)
        val vm = ProfileViewModel(repo, "test-user-id")
        advanceUntilIdle()

        vm.onEditClicked()
        val s = vm.uiState.value

        assertTrue(s.isEditing)
        assertEquals(s.savedProfile, s.draftProfile)
    }

    @Test
    fun `onDisplayNameChange updates draft when editing`() = runVmTest {
        val (pub, saved) = sampleTrips()
        val repo = TestProfileRepository(sampleProfile(displayName = "Old Name"), pub, saved)
        val vm = ProfileViewModel(repo, "test-user-id")
        advanceUntilIdle()

        vm.onEditClicked()
        vm.onDisplayNameChange("New Name")

        val s = vm.uiState.value
        assertTrue(s.isEditing)
        assertEquals("New Name", s.draftProfile!!.displayName)
        assertEquals("Old Name", s.savedProfile!!.displayName)
    }

    @Test
    fun `onBioChange does nothing when not editing`() = runVmTest {
        val (pub, saved) = sampleTrips()
        val repo = TestProfileRepository(sampleProfile(bio = "Old"), pub, saved)
        val vm = ProfileViewModel(repo, "test-user-id")
        advanceUntilIdle()

        vm.onBioChange("New")

        val s = vm.uiState.value
        assertFalse(s.isEditing)
        assertEquals("Old", s.draftProfile!!.bio)
    }

    @Test
    fun `onBioChange updates draft when editing`() = runVmTest {
        val (pub, saved) = sampleTrips()
        val repo = TestProfileRepository(sampleProfile(bio = "Old"), pub, saved)
        val vm = ProfileViewModel(repo, "test-user-id")
        advanceUntilIdle()

        vm.onEditClicked()
        vm.onBioChange("New")

        val s = vm.uiState.value
        assertTrue(s.isEditing)
        assertEquals("New", s.draftProfile!!.bio)
        // saved unchanged while editing
        assertEquals("Old", s.savedProfile!!.bio)
    }

    @Test
    fun `onCancelClicked discards edits and exits edit mode`() = runVmTest {
        val (pub, saved) = sampleTrips()
        val repo = TestProfileRepository(sampleProfile(bio = "Original"), pub, saved)
        val vm = ProfileViewModel(repo, "test-user-id")
        advanceUntilIdle()

        vm.onEditClicked()
        vm.onBioChange("Temp")
        vm.onCancelClicked()

        val s = vm.uiState.value
        assertFalse(s.isEditing)
        assertEquals("Original", s.draftProfile!!.bio)
        assertEquals("Original", s.savedProfile!!.bio)
    }

    // ─────────────────────────────────────────────
    // Save
    // ─────────────────────────────────────────────

    @Test
    fun `onSaveClicked persists draft and calls repo update`() = runVmTest {
        val (pub, saved) = sampleTrips()
        val repo = TestProfileRepository(sampleProfile(bio = "Original"), pub, saved)
        val vm = ProfileViewModel(repo, "test-user-id")
        advanceUntilIdle()

        vm.onEditClicked()
        vm.onBioChange("Saved Bio")

        vm.onSaveClicked()
        advanceUntilIdle()

        val s = vm.uiState.value
        assertFalse(s.isEditing)
        assertEquals("Saved Bio", s.savedProfile!!.bio)
        assertEquals("Saved Bio", s.draftProfile!!.bio)

        assertEquals(1, repo.updateCalls)
        assertEquals("Saved Bio", repo.lastUpdated!!.bio)
    }

    // ─────────────────────────────────────────────
    // Gender / age / tags
    // ─────────────────────────────────────────────

    @Test
    fun `onGenderSelected updates draft when editing`() = runVmTest {
        val (pub, saved) = sampleTrips()
        val repo = TestProfileRepository(sampleProfile(gender = Gender.MALE), pub, saved)
        val vm = ProfileViewModel(repo, "test-user-id")
        advanceUntilIdle()

        vm.onEditClicked()
        vm.onGenderSelected(Gender.FEMALE)

        val s = vm.uiState.value
        assertEquals(Gender.FEMALE, s.draftProfile!!.gender)
        assertEquals(Gender.MALE, s.savedProfile!!.gender)
    }

    @Test
    fun `onAgeRangeSelected updates draft when editing`() = runVmTest {
        val (pub, saved) = sampleTrips()
        val repo = TestProfileRepository(sampleProfile(ageRange = AgeRange.LATE_20S), pub, saved)
        val vm = ProfileViewModel(repo, "test-user-id")
        advanceUntilIdle()

        vm.onEditClicked()
        vm.onAgeRangeSelected(AgeRange.EARLY_30S)

        val s = vm.uiState.value
        assertEquals(AgeRange.EARLY_30S, s.draftProfile!!.ageRange)
        assertEquals(AgeRange.LATE_20S, s.savedProfile!!.ageRange)
    }

    @Test
    fun `onToggleTag adds and removes tag in draft`() = runVmTest {
        val (pub, saved) = sampleTrips()
        val repo = TestProfileRepository(sampleProfile(tags = emptySet()), pub, saved)
        val vm = ProfileViewModel(repo, "test-user-id")
        advanceUntilIdle()

        vm.onEditClicked()

        vm.onToggleTag(TravelStyleTag.CULTURE)
        assertTrue(vm.uiState.value.draftProfile!!.tags.contains(TravelStyleTag.CULTURE))

        vm.onToggleTag(TravelStyleTag.CULTURE)
        assertFalse(vm.uiState.value.draftProfile!!.tags.contains(TravelStyleTag.CULTURE))
    }

    // ─────────────────────────────────────────────
    // Avatar
    // ─────────────────────────────────────────────

    @Test
    fun `onAvatarSelected updates draft when editing with simple URL`() = runVmTest {
        val (pub, saved) = sampleTrips()
        val repo = TestProfileRepository(sampleProfile(), pub, saved)
        val vm = ProfileViewModel(repo, "test-user-id")
        advanceUntilIdle()

        vm.onEditClicked()
        vm.onAvatarSelected("https://example.com/photo.jpg")
        advanceUntilIdle()

        val s = vm.uiState.value
        assertEquals("https://example.com/photo.jpg", s.draftProfile!!.avatarUrl)
    }

    // ─────────────────────────────────────────────
    // Refresh
    // ─────────────────────────────────────────────

    @Test
    fun `refreshSavedTrips updates savedTrips in state`() = runVmTest {
        val (pub, saved) = sampleTrips()
        val repo = TestProfileRepository(
            storedProfile = sampleProfile(),
            publicTrips = pub,
            savedTrips = saved
        )

        val vm = ProfileViewModel(repo, "test-user-id")
        advanceUntilIdle()

        val newSaved = saved + TripData(
            id = "saved-2",
            title = "New Saved Trip",
            location = "London",
            imageUrl = "",
            isPublic = true,
            author = "Ben"
        )
        repo.updateSavedTrips(newSaved)

        vm.refreshSavedTrips()
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.savedTrips.size)
        assertEquals("saved-2", vm.uiState.value.savedTrips.last().id)
    }
}