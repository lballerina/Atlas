package ca.uwaterloo.atlas.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberPhotoPickerLauncher(
    allowMultiple: Boolean,
    onPhotosPicked: (List<String>) -> Unit
): () -> Unit {
    return { /* no-op on JVM */ }
}