package ca.uwaterloo.atlas.viewmodel

import ca.uwaterloo.atlas.data.repository.OtherUserProfileRepository
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherProfileViewModelTest {

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

    private class TestOtherProfileRepository(
        private var profiles: Map<String, UserProfile>,
        private var tripsByAuthor: Map<String, List<TripData>>
    ) : OtherUserProfileRepository {

        override suspend fun getProfile(authorName: String): UserProfile? =
            profiles[authorName]

        override suspend fun getPublicTrips(authorName: String): List<TripData> =
            tripsByAuthor[authorName].orEmpty()

        fun updateData(newProfiles: Map<String, UserProfile>, newTrips: Map<String, List<TripData>>) {
            profiles = newProfiles
            tripsByAuthor = newTrips
        }
    }

    private fun profile(name: String, trips: Int = 0) = UserProfile(
        displayName = name,
        avatarUrl = null,
        email = "",
        bio = "Bio for $name",
        gender = Gender.FEMALE,
        ageRange = AgeRange.EARLY_20S,
        tags = setOf(TravelStyleTag.CULTURE),
        stats = TravelStats(trips = trips, places = 0, countries = 0)
    )

    @Test
    fun `load sets isLoading true immediately then loads profile and trips`() = runVmTest {
        val author = "Sofia Martinez"

        val trips = listOf(
            TripData(id = "t1", title = "Trip 1", location = "X", imageUrl = "", isPublic = true, author = author),
            TripData(id = "t2", title = "Trip 2", location = "Y", imageUrl = "", isPublic = true, author = author)
        )

        val repo = TestOtherProfileRepository(
            profiles = mapOf(author to profile(author)),
            tripsByAuthor = mapOf(author to trips)
        )

        val vm = OtherProfileViewModel(repo)

        vm.load(author)
        assertTrue(vm.uiState.value.isLoading)

        advanceUntilIdle()

        val s = vm.uiState.value
        assertFalse(s.isLoading)
        assertNotNull(s.profile)
        assertEquals(author, s.profile.displayName)
        assertEquals(2, s.publicTrips.size)
    }

    @Test
    fun `switching users clears current data and shows loading`() = runVmTest {
        val author1 = "Sofia"
        val author2 = "John"

        val repo = TestOtherProfileRepository(
            profiles = mapOf(
                author1 to profile(author1),
                author2 to profile(author2)
            ),
            tripsByAuthor = emptyMap()
        )

        val vm = OtherProfileViewModel(repo)

        // Load user 1
        vm.load(author1)
        advanceUntilIdle()
        assertEquals(author1, vm.uiState.value.profile?.displayName)

        // Switch to user 2
        vm.load(author2)

        // Check that state is reset immediately
        val s = vm.uiState.value
        assertTrue(s.isLoading)
        assertNull(s.profile)
        assertTrue(s.publicTrips.isEmpty())

        advanceUntilIdle()
        assertEquals(author2, vm.uiState.value.profile?.displayName)
    }

    @Test
    fun `reloading same user refreshes in background without full loading state`() = runVmTest {
        val author = "Sofia"
        val originalProfile = profile(author, trips = 5)
        val updatedProfile = profile(author, trips = 6)

        val repo = TestOtherProfileRepository(
            profiles = mapOf(author to originalProfile),
            tripsByAuthor = emptyMap()
        )

        val vm = OtherProfileViewModel(repo)

        // Initial load
        vm.load(author)
        advanceUntilIdle()
        assertEquals(5, vm.uiState.value.profile?.stats?.trips)

        // Update repo data
        repo.updateData(
            newProfiles = mapOf(author to updatedProfile),
            newTrips = emptyMap()
        )

        // Reload same user
        vm.load(author)

        // Should NOT be loading (immediate data)
        assertFalse(vm.uiState.value.isLoading)
        assertEquals(5, vm.uiState.value.profile?.stats?.trips)

        advanceUntilIdle()

        // After revalidation, data should be updated
        assertEquals(6, vm.uiState.value.profile?.stats?.trips)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `load sets profile null when repository returns null`() = runVmTest {
        val author = "Unknown Person"

        val repo = TestOtherProfileRepository(
            profiles = emptyMap(),
            tripsByAuthor = emptyMap()
        )

        val vm = OtherProfileViewModel(repo)

        vm.load(author)
        advanceUntilIdle()

        val s = vm.uiState.value
        assertFalse(s.isLoading)
        assertEquals(null, s.profile)
        assertTrue(s.publicTrips.isEmpty())
    }
}