package ca.uwaterloo.atlas.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.uwaterloo.atlas.domain.place.Place

@Composable
expect fun TripMap(
    places: List<Place>,
    selectedPlace: Place?,
    modifier: Modifier = Modifier,
    onMapTapped: () -> Unit,
    onMarkerTapped: (Place) -> Unit
)