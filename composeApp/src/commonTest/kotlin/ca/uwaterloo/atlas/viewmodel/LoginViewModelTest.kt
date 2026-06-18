package ca.uwaterloo.atlas.viewmodel

import ca.uwaterloo.atlas.data.repository.MockPlaceRepository
import ca.uwaterloo.atlas.data.repository.MockUserCredentialRepository
import ca.uwaterloo.atlas.domain.credential.UserCredential
import ca.uwaterloo.atlas.domain.credential.UserCredentialModel
import ca.uwaterloo.atlas.domain.trip.TripAccessMode
import ca.uwaterloo.atlas.domain.place.CostLevel
import ca.uwaterloo.atlas.domain.place.Place
import ca.uwaterloo.atlas.domain.place.PlaceCategory
import ca.uwaterloo.atlas.domain.place.PlaceModel
import ca.uwaterloo.atlas.domain.place.TimeOfDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalDate
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

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

    // create mocks
    private val sampleUser = UserCredential(email = "testemail@test.com", password = "123456")

    private fun testUserCredentialModel(): UserCredentialModel {
        return UserCredentialModel(repository = MockUserCredentialRepository(initialUsers = listOf(sampleUser)))
    }

    private fun testLoginViewModel(): LoginViewModel {
        return LoginViewModel(model = testUserCredentialModel())
    }

    @Test
    fun `email change`() {
        val vm = testLoginViewModel()
        vm.onEmailChange("new@test.com")
        val state = vm.uiState.value
        assertEquals("new@test.com", state.email)
    }

    @Test
    fun `password change`() {
        val vm = testLoginViewModel()
        vm.onPasswordChange("newpassword")
        val state = vm.uiState.value
        assertEquals("newpassword", state.password)
    }

    @Test
    fun `missing or blank field`() = runVmTest {
        val vm = testLoginViewModel()
        vm.onEmailChange("")
        vm.onPasswordChange("")
        vm.tryLogin()
        advanceUntilIdle()

        // should be error state
        val state = vm.uiState.value
        assertTrue(state.loginError)
        assertFalse(state.loginSuccess)
        assertFalse(state.isLoading)
    }
}