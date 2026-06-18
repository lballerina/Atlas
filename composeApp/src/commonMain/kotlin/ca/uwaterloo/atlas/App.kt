package ca.uwaterloo.atlas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ca.uwaterloo.atlas.data.repository.OtherUserProfileRepository
import ca.uwaterloo.atlas.data.repository.RemoteOtherUserProfileRepository
import ca.uwaterloo.atlas.data.repository.RemotePlaceRepository
import ca.uwaterloo.atlas.data.repository.RemoteTripRepository
import ca.uwaterloo.atlas.data.repository.RemoteUserCredentialRepository
import ca.uwaterloo.atlas.data.repository.RemoteUserProfileRepository
import ca.uwaterloo.atlas.data.repository.UserProfileRepository
import ca.uwaterloo.atlas.domain.credential.UserCredentialModel
import ca.uwaterloo.atlas.domain.place.PlaceModel
import ca.uwaterloo.atlas.domain.trip.TripAccessMode
import ca.uwaterloo.atlas.domain.trip.TripModel
import ca.uwaterloo.atlas.navigation.Screen
import ca.uwaterloo.atlas.platform.PlatformImage
import ca.uwaterloo.atlas.ui.components.BottomNavigationBar
import ca.uwaterloo.atlas.ui.screens.ExploreScreen
import ca.uwaterloo.atlas.ui.screens.LoginScreen
import ca.uwaterloo.atlas.ui.screens.MyTripsScreen
import ca.uwaterloo.atlas.ui.screens.OtherProfileScreen
import ca.uwaterloo.atlas.ui.screens.ProfileScreen
import ca.uwaterloo.atlas.ui.screens.SignupScreen
import ca.uwaterloo.atlas.ui.screens.SinglePinScreen
import ca.uwaterloo.atlas.ui.screens.ViewSingleTripScreen
import ca.uwaterloo.atlas.ui.theme.AtlasTheme
import ca.uwaterloo.atlas.viewmodel.ExploreViewModel
import ca.uwaterloo.atlas.viewmodel.LoginViewModel
import ca.uwaterloo.atlas.viewmodel.MyTripsViewModel
import ca.uwaterloo.atlas.viewmodel.OtherProfileViewModel
import ca.uwaterloo.atlas.viewmodel.ProfileViewModel
import ca.uwaterloo.atlas.viewmodel.SignupViewModel
import ca.uwaterloo.atlas.viewmodel.SinglePinViewModel
import ca.uwaterloo.atlas.viewmodel.SingleTripViewModel

@Composable
fun App() {
    AtlasTheme {
        // ── Navigation state ──────────────────────────────────────────────
        var currentScreen          by remember { mutableStateOf(Screen.LOGIN) }
        var previousScreen         by remember { mutableStateOf<Screen?>(null) }
        var selectedTripId         by remember { mutableStateOf<String?>(null) }
        var selectedTripAccessMode by remember { mutableStateOf(TripAccessMode.EDIT) }
        var selectedPlaceId        by remember { mutableStateOf<String?>(null) }
        var selectedAuthor         by remember { mutableStateOf<String?>(null) }

        // ── Backend toggle ────────────────────────────────────────────────
        // MOCK MODE:   uncomment Mock* lines, comment Remote* lines.
        // REMOTE MODE: uncomment Remote* lines, comment Mock* lines.
        // All six repos must be toggled together.
        // ─────────────────────────────────────────────────────────────────

        // ── Auth (created once, before any userId is known) ───────────────
        // val credRepo = remember { MockUserCredentialRepository() }
        val credRepo = remember { RemoteUserCredentialRepository() }
        val credModel = remember { UserCredentialModel(repository = credRepo) }

        val loginViewModel  = remember { LoginViewModel(credModel) }
        val loginState      by loginViewModel.uiState.collectAsState()

        val signupViewModel = remember { SignupViewModel(credModel) }
        val signupState     by signupViewModel.uiState.collectAsState()

        // ── Current user ID ───────────────────────────────────────────────
        // Empty string = not logged in. All data repos keyed on this so they
        // rebuild when the user changes. Blank userId never reaches Supabase
        // because every init block and query guards against isBlank().
        var currentUserId by remember { mutableStateOf("") }

        LaunchedEffect(loginState.loginSuccess, loginState.userId) {
            if (loginState.loginSuccess && loginState.userId != null) {
                currentUserId = loginState.userId!!
                currentScreen = Screen.MY_TRIPS
            }
        }
        LaunchedEffect(signupState.signupSuccess, signupState.userId) {
            if (signupState.signupSuccess && signupState.userId != null) {
                currentUserId = signupState.userId!!
                currentScreen = Screen.MY_TRIPS
            }
        }

        // ── Auth screens — rendered before any userId is known ────────────
        // Return early here so the data repos/ViewModels below are never
        // instantiated (and never fire Supabase queries) until after login.
        if (currentUserId.isBlank()) {
            when (currentScreen) {
                Screen.SIGNUP -> SignupScreen(
                    vm            = signupViewModel,
                    onSwitchClick = { signupViewModel.reset(); currentScreen = Screen.LOGIN }
                )
                else -> LoginScreen(
                    vm            = loginViewModel,
                    onSwitchClick = { loginViewModel.reset(); currentScreen = Screen.SIGNUP }
                )
            }
            return@AtlasTheme
        }

        // ── Everything below only runs after a real userId is set ─────────

        // val tripRepo  = remember(currentUserId) { MockTripRepository(currentUserId = currentUserId) }
        // val placeRepo = remember(currentUserId) { MockPlaceRepository() }
        val tripRepo  = remember(currentUserId) { RemoteTripRepository(currentUserId = currentUserId) }
        val placeRepo = remember(currentUserId) { RemotePlaceRepository() }

        val tripModel  = remember(currentUserId) {
            TripModel(repository = tripRepo, currentUserId = currentUserId)
        }
        val placeModel = remember(currentUserId) {
            PlaceModel(repository = placeRepo)
        }

        // val profileRepo: UserProfileRepository =
        //     remember(currentUserId) { MockUserProfileRepository(tripModel) }
        // val otherProfileRepo: OtherUserProfileRepository =
        //     remember(currentUserId) { MockOtherUserProfileRepository(tripModel) }
        val profileRepo: UserProfileRepository =
            remember(currentUserId) { RemoteUserProfileRepository() }
        val otherProfileRepo: OtherUserProfileRepository =
            remember(currentUserId) { RemoteOtherUserProfileRepository() }

        val myTripsViewModel  = remember(currentUserId) { MyTripsViewModel(tripModel) }
        val exploreViewModel  = remember(currentUserId) { ExploreViewModel(tripModel) }
        val profileViewModel  = remember(currentUserId) {
            ProfileViewModel(repo = profileRepo, userId = currentUserId)
        }
        val otherProfileViewModel = remember(currentUserId) {
            OtherProfileViewModel(repo = otherProfileRepo)
        }

        // ── Scaffold ──────────────────────────────────────────────────────
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            bottomBar = {
                if (currentScreen != Screen.LOGIN &&
                    currentScreen != Screen.SIGNUP &&
                    currentScreen != Screen.SINGLE_TRIP &&
                    currentScreen != Screen.SINGLE_PIN
                ) {
                    BottomNavigationBar(
                        currentScreen = currentScreen,
                        onScreenSelected = { screen ->
                            currentScreen = screen
                            if (screen != Screen.EXPLORE) selectedAuthor = null
                        }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                when (currentScreen) {

                    Screen.LOGIN -> {
                        LoginScreen(
                            vm            = loginViewModel,
                            onSwitchClick = { loginViewModel.reset(); currentScreen = Screen.SIGNUP }
                        )
                    }

                    Screen.SIGNUP -> {
                        SignupScreen(
                            vm            = signupViewModel,
                            onSwitchClick = { signupViewModel.reset(); currentScreen = Screen.LOGIN }
                        )
                    }

                    Screen.MY_TRIPS -> {
                        MyTripsScreen(
                            trips               = myTripsViewModel.filteredTrips,
                            searchQuery         = myTripsViewModel.searchQuery,
                            onSearchQueryChange = { myTripsViewModel.updateSearchQuery(it) },
                            activeFilters       = myTripsViewModel.activeFilters,
                            availableTags       = myTripsViewModel.availableTags,
                            onApplyFilters      = { myTripsViewModel.applyFilters(it) },
                            onTripClick         = { trip ->
                                previousScreen         = Screen.MY_TRIPS
                                selectedTripId         = trip.id
                                selectedTripAccessMode = TripAccessMode.EDIT
                                currentScreen          = Screen.SINGLE_TRIP
                            },
                            onSaveTrip   = { tripData -> myTripsViewModel.createTrip(tripData) },
                            onEditTrip   = { trip, tripData -> myTripsViewModel.updateTrip(trip, tripData) },
                            onDeleteTrip = { trip -> myTripsViewModel.deleteTrip(trip.id) }
                        )
                    }

                    Screen.EXPLORE -> {
                        if (selectedAuthor == null) {
                            ExploreScreen(
                                trips               = exploreViewModel.filteredTrips,
                                searchQuery         = exploreViewModel.searchQuery,
                                onSearchQueryChange = { exploreViewModel.updateSearchQuery(it) },
                                activeFilters       = exploreViewModel.activeFilters,
                                availableTags       = exploreViewModel.availableTags,
                                onApplyFilters      = { exploreViewModel.applyFilters(it) },
                                onTripClick         = { trip ->
                                    previousScreen         = Screen.EXPLORE
                                    selectedTripId         = trip.id
                                    selectedTripAccessMode = TripAccessMode.VIEW_ONLY
                                    currentScreen          = Screen.SINGLE_TRIP
                                },
                                onAuthorClick = { authorName -> selectedAuthor = authorName },
                                savedTripIds  = exploreViewModel.savedTripIds,
                                onToggleSave  = { tripId ->
                                    exploreViewModel.toggleSave(tripId)
                                    profileViewModel.refreshSavedTrips()
                                }
                            )
                        } else {
                            OtherProfileScreen(
                                authorName = selectedAuthor!!,
                                onBack     = { selectedAuthor = null },
                                vm         = otherProfileViewModel,
                                onTripClick = { trip ->
                                    previousScreen         = Screen.EXPLORE
                                    selectedTripId         = trip.id
                                    selectedTripAccessMode = TripAccessMode.VIEW_ONLY
                                    currentScreen          = Screen.SINGLE_TRIP
                                }
                            )
                        }
                    }

                    Screen.PROFILE -> {
                        ProfileScreen(vm = profileViewModel,
                            onTripClick = { trip ->
                                previousScreen = Screen.PROFILE
                                selectedTripId = trip.id
                                selectedTripAccessMode = TripAccessMode.VIEW_ONLY
                                currentScreen = Screen.SINGLE_TRIP
                            },
                            onSavedTripClick = { trip ->
                                previousScreen = Screen.PROFILE
                                selectedTripId = trip.id
                                selectedTripAccessMode = TripAccessMode.VIEW_ONLY
                                currentScreen = Screen.SINGLE_TRIP
                            },
                            onLogoutClick = {
                                loginViewModel.reset()
                                signupViewModel.reset()
                                currentScreen = Screen.LOGIN
                            },
                        )
                    }

                    Screen.SINGLE_TRIP -> {
                        val singleTripViewModel = remember(
                            currentUserId, selectedTripId, selectedTripAccessMode
                        ) {
                            SingleTripViewModel(placeModel, tripModel, selectedTripAccessMode)
                        }

                        LaunchedEffect(selectedTripId) {
                            selectedTripId?.let { singleTripViewModel.loadTrip(it) }
                        }

                        ViewSingleTripScreen(
                            trip       = singleTripViewModel.currentTrip,
                            places     = singleTripViewModel.places,
                            isEditable = singleTripViewModel.isEditable,
                            onBackClick = {
                                currentScreen = previousScreen ?: Screen.MY_TRIPS
                                selectedTripId = null
                                previousScreen = null
                            },
                            onAddPlace = { placeData ->
                                singleTripViewModel.addPlace(
                                    name           = placeData.name,
                                    category       = placeData.category,
                                    latitude       = placeData.latitude,
                                    longitude      = placeData.longitude,
                                    address        = placeData.address,
                                    photos         = placeData.photos,
                                    thumbnailPhoto = placeData.thumbnailPhoto,
                                    notes          = placeData.notes,
                                    dateVisited    = placeData.dateVisited,
                                    rating         = placeData.rating,
                                    mood           = placeData.mood,
                                    tags           = placeData.tags,
                                    costIndicator  = placeData.costIndicator,
                                    costAmount     = placeData.costAmount,
                                    timeOfDay      = placeData.timeOfDay,
                                    photoCaptions  = placeData.photoCaptions,
                                    isFavorite     = placeData.isFavorite
                                )
                            },
                            onPlaceClick = { place ->
                                selectedPlaceId = place.id
                                currentScreen   = Screen.SINGLE_PIN
                            },
                            onDeletePlaces = { ids ->
                                ids.forEach { singleTripViewModel.deletePlace(it) }
                            },
                            onSortSelected  = { singleTripViewModel.updateSort(it) },
                            onFilterChanged = { singleTripViewModel.updateFilter(it) },
                            onSearchChanged = { singleTripViewModel.updateSearchQuery(it) }
                        )
                    }

                    Screen.SINGLE_PIN -> {
                        selectedPlaceId?.let { placeId ->
                            val singlePinViewModel = remember(currentUserId, placeId) {
                                SinglePinViewModel(placeModel, selectedTripAccessMode)
                            }

                            LaunchedEffect(placeId) {
                                singlePinViewModel.loadPlace(placeId)
                            }

                            SinglePinScreen(
                                placeId     = placeId,
                                vm          = singlePinViewModel,
                                onBackClick = {
                                    currentScreen   = Screen.SINGLE_TRIP
                                    selectedPlaceId = null
                                },
                                onDeletePin = {
                                    currentScreen   = Screen.SINGLE_TRIP
                                    selectedPlaceId = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
