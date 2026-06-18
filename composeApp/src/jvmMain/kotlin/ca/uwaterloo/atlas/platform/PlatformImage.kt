package ca.uwaterloo.atlas.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box

@Composable
actual fun PlatformImage(
    uri: String,
    modifier: Modifier,
    contentScale: androidx.compose.ui.layout.ContentScale
) {
    // Simple placeholder for JVM
    Box(modifier = modifier)
}