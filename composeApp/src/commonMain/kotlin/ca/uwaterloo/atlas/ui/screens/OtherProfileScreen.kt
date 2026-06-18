package ca.uwaterloo.atlas.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atlas.composeapp.generated.resources.Res
import atlas.composeapp.generated.resources.sofia
import ca.uwaterloo.atlas.domain.profile.TravelStyleTag
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.platform.PlatformImage
import ca.uwaterloo.atlas.ui.components.TripGrid
import ca.uwaterloo.atlas.viewmodel.OtherProfileViewModel
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OtherProfileScreen(
    authorName: String,
    onBack: () -> Unit,
    vm: OtherProfileViewModel,
    onTripClick: ((TripData) -> Unit)? = null
) {
    val state by vm.uiState.collectAsState()
    val profile = state.profile
    val scrollState = rememberScrollState()

    LaunchedEffect(authorName) {
        vm.load(authorName)
    }

    if (state.isLoading || (profile == null && state.isLoading)) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF4C5BD4))
        }
        return
    }

    Scaffold(
        containerColor = Color(0xFFF7F7FB)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(Color(0xFFF7F7FB))
                .padding(bottom = paddingValues.calculateBottomPadding())
                .navigationBarsPadding()
        ) {

            // Gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF4C5BD4),
                                Color(0xFF8D6E95),
                                Color(0xFFC79AA1)
                            )
                        )
                    )
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Traveler Profile",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Main profile card area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-54).dp)
                    .padding(horizontal = 16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                                .shadow(4.dp, CircleShape)
                        ) {
                            OtherProfileAvatar(
                                avatarUrl = profile?.avatarUrl,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = profile?.displayName ?: authorName,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F1F1F)
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            profile?.let {
                                SummaryPill("👤", it.gender.label)
                                Spacer(Modifier.width(10.dp))
                                SummaryPill("🎂", it.ageRange.label)
                            }
                        }

                        Spacer(Modifier.height(18.dp))
                        
                        Text(
                            text = profile?.bio?.ifBlank { "This traveler hasn't added a bio yet." } 
                                ?: "No information available.",
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = if (profile?.bio.isNullOrBlank()) Color.Gray else Color(0xFF4A4A4A),
                            textAlign = TextAlign.Center
                        )

                        if (profile != null && profile.tags.isNotEmpty()) {
                            Spacer(Modifier.height(20.dp))
                            OtherTravelTagSection(tags = profile.tags)
                        }

                        Spacer(Modifier.height(24.dp))

                        profile?.let {
                            OtherStatsCard(
                                trips = it.stats.trips,
                                places = it.stats.places,
                                countries = it.stats.countries
                            )
                        }
                    }
                }
            }

            // Public Trips Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-34).dp)
            ) {
                OtherSectionCard(
                    title = "Public Trips",
                    subtitle = "Adventures shared by this traveler"
                ) {
                    if (state.publicTrips.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No public trips shared yet.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        TripGrid(
                            trips = state.publicTrips,
                            onTripClick = { onTripClick?.invoke(it) }
                        )
                    }
                }
                
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SummaryPill(icon: String, text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFFF3F4F8)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444)
            )
        }
    }
}

@Composable
private fun OtherStatsCard(
    trips: Int,
    places: Int,
    countries: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFF8F5FF)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OtherStatItem(number = trips.toString(), label = "Trips")
            OtherStatItem(number = places.toString(), label = "Places")
            OtherStatItem(number = countries.toString(), label = "Countries")
        }
    }
}

@Composable
private fun OtherStatItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4C5BD4)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF777777)
        )
    }
}

@Composable
private fun OtherSectionCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1F1F)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color(0xFF8A8A8A)
            )
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OtherTravelTagSection(tags: Set<TravelStyleTag>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Travel Style",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2A2A2A)
        )

        Spacer(Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            tags.forEach { tag ->
                OtherTravelTag(label = tag.label)
            }
        }
    }
}

@Composable
private fun OtherTravelTag(
    label: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFFF1F4FF)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4C5BD4)
        )
    }
}

@Composable
private fun OtherProfileAvatar(
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    if (avatarUrl.isNullOrBlank()) {
        Image(
            painter = painterResource(Res.drawable.sofia),
            contentDescription = "Avatar",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        PlatformImage(
            uri = avatarUrl,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}