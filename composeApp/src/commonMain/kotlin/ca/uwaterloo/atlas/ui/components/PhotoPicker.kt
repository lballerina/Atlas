package ca.uwaterloo.atlas.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ca.uwaterloo.atlas.platform.rememberPhotoPickerLauncher
import ca.uwaterloo.atlas.platform.PlatformImage

data class SelectedPhoto(
    val id: String,
    val uri: String,
    val caption: String = ""
)

@Composable
fun PhotoPickerGrid(
    photos: List<SelectedPhoto>,
    coverPhotoId: String?,
    onPhotosSelected: (List<SelectedPhoto>) -> Unit,
    onPhotoRemoved: (SelectedPhoto) -> Unit,
    onCoverPhotoSelected: (String) -> Unit,
    maxPhotos: Int = 10,
    showCoverSelection: Boolean = true,
    modifier: Modifier = Modifier
) {

    // Real Android picker launcher
    val launchPicker = rememberPhotoPickerLauncher { uriStrings ->

        val remainingSlots = maxPhotos - photos.size
        val limitedUris = uriStrings.take(remainingSlots)

        val newPhotos = limitedUris.map {
            SelectedPhoto(
                id = it,
                uri = it
            )
        }

        if (newPhotos.isNotEmpty()) {
            onPhotosSelected(photos + newPhotos)

            // Auto-select first photo as cover if none selected
            if (coverPhotoId == null) {
                onCoverPhotoSelected(newPhotos.first().id)
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Add Photos Button
        if (photos.size < maxPhotos) {
            OutlinedButton(
                onClick = launchPicker,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Gray.copy(alpha = 0.05f)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = "Add Photos",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (photos.isEmpty()) "Add Photos" else "Add More Photos",
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${photos.size}/$maxPhotos photos",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Cover instructions
        if (showCoverSelection && photos.isNotEmpty()) {
            Text(
                text = "Tap the star to set a cover photo",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Grid
        if (photos.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.heightIn(max = 400.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(photos, key = { _, photo -> photo.id }) { index, photo ->

                    PhotoGridItem(
                        photo = photo,
                        isCover = photo.id == coverPhotoId,
                        showCoverSelection = showCoverSelection,
                        onRemove = { onPhotoRemoved(photo) },
                        onSetCover = { onCoverPhotoSelected(photo.id) },
                        onMoveLeft = {
                            if (index > 0) {
                                val mutable = photos.toMutableList()
                                mutable.removeAt(index)
                                mutable.add(index - 1, photo)
                                onPhotosSelected(mutable)
                            }
                        },
                        onMoveRight = {
                            if (index < photos.lastIndex) {
                                val mutable = photos.toMutableList()
                                mutable.removeAt(index)
                                mutable.add(index + 1, photo)
                                onPhotosSelected(mutable)
                            }
                        },
                        onCaptionChange = { newCaption ->
                            val updatedList = photos.map {
                                if (it.id == photo.id) {
                                    it.copy(caption = newCaption)
                                } else {
                                    it
                                }
                            }
                            onPhotosSelected(updatedList)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoGridItem(
    photo: SelectedPhoto,
    isCover: Boolean,
    showCoverSelection: Boolean,
    onRemove: () -> Unit,
    onSetCover: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onCaptionChange: (String) -> Unit
) {
    var showFullscreen by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .border(
                width = if (isCover) 3.dp else 0.dp,
                color = if (isCover) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            PlatformImage(
                uri = photo.uri,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showFullscreen = true },
                contentScale = ContentScale.Crop
            )

            if (isCover) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Cover",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                // Move Left
                IconButton(
                    onClick = onMoveLeft,
                    modifier = Modifier.size(28.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Move left",
                            modifier = Modifier
                                .padding(4.dp)
                                .size(16.dp)
                        )
                    }
                }

                // Move Right
                IconButton(
                    onClick = onMoveRight,
                    modifier = Modifier.size(28.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Move right",
                            modifier = Modifier
                                .padding(4.dp)
                                .size(16.dp)
                        )
                    }
                }

                // Cover toggle
                if (showCoverSelection) {
                    IconButton(
                        onClick = onSetCover,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.9f)
                        ) {
                            Icon(
                                if (isCover) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Set as cover",
                                tint = if (isCover) Color(0xFFFFD700) else Color.Gray,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                }

                // Remove button
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = Color.Red,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showFullscreen) {
        PhotoFullscreenDialog(
            photo = photo,
            onDismiss = { showFullscreen = false },
            onCaptionChanged = { newCaption ->
                onCaptionChange(newCaption)
            }
        )
    }
}

@Composable
fun PhotoFullscreenDialog(
    photo: SelectedPhoto,
    onDismiss: () -> Unit,
    onCaptionChanged: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column {
            PlatformImage(
                uri = photo.uri,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )

            OutlinedTextField(
                value = photo.caption,
                onValueChange = onCaptionChanged,
                label = { Text("Caption") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f)
                )
            )

            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    }
}