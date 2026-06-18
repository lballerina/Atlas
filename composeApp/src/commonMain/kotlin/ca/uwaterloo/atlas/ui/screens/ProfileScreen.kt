package ca.uwaterloo.atlas.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import atlas.composeapp.generated.resources.andrew
import ca.uwaterloo.atlas.domain.profile.AgeRange
import ca.uwaterloo.atlas.domain.profile.Gender
import ca.uwaterloo.atlas.domain.profile.TravelStyleTag
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.platform.PlatformImage
import ca.uwaterloo.atlas.platform.rememberPhotoPickerLauncher
import ca.uwaterloo.atlas.ui.components.TripGrid
import ca.uwaterloo.atlas.viewmodel.ProfileViewModel
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    vm: ProfileViewModel,
    onPublicViewClick: (() -> Unit)? = null,
    onTripClick: ((TripData) -> Unit)? = null,
    onSavedTripClick: ((TripData) -> Unit)? = null,
    onLogoutClick: (() -> Unit),
) {
    val state by vm.uiState.collectAsState()
    val profile = state.draftProfile
    val isEditing = state.isEditing
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        vm.refreshProfile()
    }

    if (state.isLoading || profile == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
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
            // Header with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
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
                        .padding(horizontal = 18.dp)
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row() {
                        PlatformImage(
                            uri = "https://ulcxyvywffoxuafjjszo.supabase.co/storage/v1/object/public/photos/smallLogoTransparent.png",
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .size(40.dp),
                            contentScale = ContentScale.Inside
                        )
                        Text(
                            text = "Profile",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isEditing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onPublicViewClick != null) {
                                ProfileActionChip(
                                    text = "Public View",
                                    onClick = onPublicViewClick
                                )
                                Spacer(Modifier.width(8.dp))
                            }

                            ProfileActionChip(
                                text = "Edit",
                                icon = { Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = Color.White) },
                                onClick = { vm.onEditClicked() }
                            )

                            Spacer(Modifier.width(8.dp))

                            LogoutButton(logout = onLogoutClick)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { vm.onCancelClicked() }) {
                                Text("Cancel", color = Color.White)
                            }
                            Spacer(Modifier.width(6.dp))
                            Button(
                                onClick = { vm.onSaveClicked() },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF4C5BD4)
                                )
                            ) {
                                Text("Save", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
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
                        .shadow(10.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!isEditing) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, Color.White, CircleShape)
                            ) {
                                ProfileAvatar(
                                    avatarUrl = profile.avatarUrl,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = profile.displayName,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F1F1F)
                            )

                            Spacer(Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SummaryPill("👤", profile.gender.label)
                                Spacer(Modifier.width(10.dp))
                                SummaryPill("🎂", profile.ageRange.label)
                            }

                            Spacer(Modifier.height(14.dp))
                        }
                        if (!isEditing) {
                            Text(
                                text = profile.bio.ifBlank { "No bio yet." },
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = if (profile.bio.isBlank()) Color.Gray else Color(0xFF4A4A4A),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            EditModeProfileBlock(
                                avatarUrl = profile.avatarUrl,
                                displayName = profile.displayName,
                                bio = profile.bio,
                                gender = profile.gender,
                                ageRange = profile.ageRange,
                                tags = profile.tags,
                                onAvatarSelected = vm::onAvatarSelected,
                                onDisplayNameChange = vm::onDisplayNameChange,
                                onBioChange = vm::onBioChange,
                                onGenderSelected = vm::onGenderSelected,
                                onAgeRangeSelected = vm::onAgeRangeSelected,
                                onToggleTag = vm::onToggleTag
                            )
                        }

                        if (!isEditing && profile.tags.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            TravelTagSection(tags = profile.tags)
                        }

                        if (!isEditing) {
                            Spacer(Modifier.height(22.dp))

                            StatsCard(
                                trips = profile.stats.trips,
                                places = profile.stats.places,
                                countries = profile.stats.countries
                            )
                        }
                    }
                }
            }

            if (!isEditing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-34).dp)
                ) {

                    SectionCard(
                        title = "Public Trips",
                        subtitle = "Trips you shared with others"
                    ) {
                        TripGrid(
                            trips = state.publicTrips,
                            onTripClick = { onTripClick?.invoke(it) }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    SectionCard(
                        title = "Saved Trips",
                        subtitle = "Trips you bookmarked"
                    ) {
                        TripGrid(
                            trips = state.savedTrips,
                            onTripClick = { onSavedTripClick?.invoke(it) }
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileActionChip(
    text: String,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        )
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(6.dp))
        }
        Text(text, color = Color.White)
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
private fun StatsCard(
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
            StatItem(number = trips.toString(), label = "Trips")
            StatItem(number = places.toString(), label = "Places")
            StatItem(number = countries.toString(), label = "Countries")
        }
    }
}

@Composable
private fun StatItem(number: String, label: String) {
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
private fun SectionCard(
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
private fun TravelTagSection(tags: Set<TravelStyleTag>) {
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
                TravelTag(label = tag.label)
            }
        }
    }
}

@Composable
private fun TravelTag(
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
private fun EditModeProfileBlock(
    avatarUrl: String?,
    displayName: String,
    bio: String,
    gender: Gender,
    ageRange: AgeRange,
    tags: Set<TravelStyleTag>,
    onAvatarSelected: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onGenderSelected: (Gender) -> Unit,
    onAgeRangeSelected: (AgeRange) -> Unit,
    onToggleTag: (TravelStyleTag) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        SectionLabel("Profile Photo")

        AvatarEditor(
            avatarUrl = avatarUrl,
            onAvatarSelected = onAvatarSelected
        )

        Spacer(Modifier.height(14.dp))

        SectionLabel("Display Name")
        EditableField(
            value = displayName,
            enabled = true,
            singleLine = true,
            onValueChange = onDisplayNameChange
        )

        Spacer(Modifier.height(14.dp))

        SectionLabel("Bio")
        EditableField(
            value = bio,
            enabled = true,
            minLines = 3,
            onValueChange = onBioChange
        )

        Spacer(Modifier.height(14.dp))

        SectionLabel("Gender")
        EnumDropdownField(
            valueLabel = gender.label,
            options = Gender.entries.map { it.label },
            enabled = true,
            onSelectIndex = { idx -> onGenderSelected(Gender.entries[idx]) }
        )

        Spacer(Modifier.height(14.dp))

        SectionLabel("Age Range")
        EnumDropdownField(
            valueLabel = ageRange.label,
            options = AgeRange.entries.map { it.label },
            enabled = true,
            onSelectIndex = { idx -> onAgeRangeSelected(AgeRange.entries[idx]) }
        )

        Spacer(Modifier.height(18.dp))

        SectionLabel("Travel Style Tags")
        Text(
            text = "Select all that apply to help others find relatable trips",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TravelStyleTag.entries.forEach { tag ->
                val selected = tags.contains(tag)
                FilterChip(
                    selected = selected,
                    onClick = { onToggleTag(tag) },
                    label = { Text(tag.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4C5BD4),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF1F1F1F)
    )
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun EditableField(
    value: String,
    enabled: Boolean,
    singleLine: Boolean = false,
    minLines: Int = 1,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnumDropdownField(
    valueLabel: String,
    options: List<String>,
    enabled: Boolean,
    onSelectIndex: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = valueLabel,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                    enabled = enabled
                ),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { idx, label ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        expanded = false
                        onSelectIndex(idx)
                    }
                )
            }
        }
    }
}

@Composable
private fun LogoutButton(
    logout: () -> Unit,
) {
    var confirmDialog by remember { mutableStateOf(false) }
    ProfileActionChip(
        text = "Logout",
        icon = {Icon(Icons.Outlined.Logout, contentDescription = "Logout", tint = Color.White) },
        onClick = { confirmDialog = true }
    )

    // modal to confirm logout
    if (confirmDialog) {
        AlertDialog(
            onDismissRequest = { confirmDialog = false },
            title = {
                Text("Heading home?")
            },
            text = {
                Text("All unsaved changes will be lost!")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDialog = false
                        logout()
                    }
                ) {
                    Text("Log out")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileAvatar(
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    if (avatarUrl.isNullOrBlank()) {
        Image(
            painter = painterResource(Res.drawable.andrew),
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

@Composable
private fun AvatarEditor(
    avatarUrl: String?,
    onAvatarSelected: (String) -> Unit
) {
    val launchPicker = rememberPhotoPickerLauncher(
        allowMultiple = false
    ) { uriStrings ->
        uriStrings.firstOrNull()?.let { pickedUri ->
            onAvatarSelected(pickedUri)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .border(2.dp, Color(0xFFE5E7EB), CircleShape)
        ) {
            ProfileAvatar(
                avatarUrl = avatarUrl,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Choose a new profile photo",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = launchPicker,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "Change photo"
            )
            Spacer(Modifier.width(8.dp))
            Text("Change Photo")
        }
    }
}