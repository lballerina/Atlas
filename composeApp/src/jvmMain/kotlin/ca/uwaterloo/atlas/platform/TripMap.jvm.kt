package ca.uwaterloo.atlas.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import ca.uwaterloo.atlas.domain.place.Place

@Composable
actual fun TripMap(
    places: List<Place>,
    selectedPlace: Place?,
    modifier: Modifier,
    onMapTapped: () -> Unit,
    onMarkerTapped: (Place) -> Unit
) {
    Box(
        modifier = modifier
            .background(Color(0xFFE8F4F8)),
        contentAlignment = Alignment.Center
    ) {
        Text("Map only available on Android")
    }
}