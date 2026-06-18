package ca.uwaterloo.atlas.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
actual fun PlatformImage(
    uri: String,
    modifier: Modifier,
    contentScale: ContentScale
) {
    AsyncImage(
        model = uri,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale,
        onError = { println("Image couldn't be loaded: $uri") }
    )
}