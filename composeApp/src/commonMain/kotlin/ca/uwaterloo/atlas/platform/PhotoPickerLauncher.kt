package ca.uwaterloo.atlas.platform

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPhotoPickerLauncher(
    allowMultiple: Boolean = true,
    onPhotosPicked: (List<String>) -> Unit
): () -> Unit