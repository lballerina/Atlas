package ca.uwaterloo.atlas.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

@Composable
expect fun PlatformImage(
    uri: String,
    modifier: Modifier,
    contentScale: ContentScale
)