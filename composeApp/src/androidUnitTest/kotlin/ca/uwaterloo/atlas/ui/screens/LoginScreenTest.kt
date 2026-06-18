package ca.uwaterloo.atlas.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.ui.components.TripFilters
import ca.uwaterloo.atlas.ui.theme.AtlasTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import ca.uwaterloo.atlas.data.repository.MockUserCredentialRepository
import ca.uwaterloo.atlas.domain.credential.UserCredential
import ca.uwaterloo.atlas.domain.credential.UserCredentialModel
import ca.uwaterloo.atlas.viewmodel.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.collections.get
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI tests for [LoginScreen]
 */

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // create mocks
    private val sampleUser = UserCredential(email = "testemail@test.com", password = "123456")

    private fun testUserCredentialModel(): UserCredentialModel {
        return UserCredentialModel(repository = MockUserCredentialRepository(initialUsers = listOf(sampleUser)))
    }

    private fun testLoginViewModel(): LoginViewModel {
        return LoginViewModel(model = testUserCredentialModel())
    }

    @Test
    fun `confirm text is showing`() {
        composeTestRule.setContent {
            LoginScreen(
                vm = testLoginViewModel(),
                onSwitchClick = { null }
            )
        }
        composeTestRule.onNodeWithText("ATLAS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome\nBack").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign in to your account").assertIsDisplayed()
    }

    // note: difficult to unit test the actual switch because the state is reset upon click
    // so we just test that it can be clicked as a unit test
    @Test
    fun `switch button is clickable`() {
        composeTestRule.setContent {
            LoginScreen(
                vm = testLoginViewModel(),
                onSwitchClick = { }
            )
        }

        // select the TAG and ensure it is clickable
        composeTestRule.onNodeWithTag("switchSignup").assertHasClickAction()
    }

    @Test
    fun `input fields are working`() {
        composeTestRule.setContent {
            LoginScreen(
                vm = testLoginViewModel(),
                onSwitchClick = { }
            )
        }

        // select the TAG and ensure it is inputtable
        composeTestRule.onNodeWithTag("emailInput").assertIsEnabled().assert(hasSetTextAction())
        composeTestRule.onNodeWithTag("passwordInput").assertIsEnabled().assert(hasSetTextAction())
    }
}