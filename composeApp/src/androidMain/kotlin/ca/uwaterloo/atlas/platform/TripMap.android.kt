package ca.uwaterloo.atlas.platform

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import ca.uwaterloo.atlas.domain.place.Place
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.BoundingBox

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
actual fun TripMap(
    places: List<Place>,
    selectedPlace: Place?,
    modifier: Modifier,
    onMapTapped: () -> Unit,
    onMarkerTapped: (Place) -> Unit
) {

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osm", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setUseDataConnection(true)
        }
    }

    var firstLoad by remember { mutableStateOf(true) }
    LaunchedEffect(places.size) {
        firstLoad = true
    }
    LaunchedEffect(selectedPlace) {

        selectedPlace?.let { place ->

            val offsetLat = place.latitude - 0.0045
            val adjustedPoint = GeoPoint(offsetLat, place.longitude)

            mapView.controller.animateTo(
                adjustedPoint,
                15.0,
                600L
            )
        }
    }
    val markers = remember { mutableMapOf<String, Marker>() }
    var lastSelectedId by remember { mutableStateOf<String?>(null) }
    val baseMarkerIcon = remember {
        context.getDrawable(org.osmdroid.library.R.drawable.marker_default)
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { mapView ->

                // remove markers that no longer exist
                val existingIds = places.map { it.id }.toSet()

                markers.keys
                    .filter { it !in existingIds }
                    .forEach { removedId ->

                        val marker = markers.remove(removedId)

                        marker?.let {
                            mapView.overlays.remove(it)
                        }
                    }

                val geoPoints = places.map {
                    GeoPoint(it.latitude, it.longitude)
                }

                places.forEach { place ->

                    val marker = markers.getOrPut(place.id) {

                        Marker(mapView).apply {
                            position = GeoPoint(place.latitude, place.longitude)
                            title = place.name

                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                            icon = mapView.context.getDrawable(org.osmdroid.library.R.drawable.marker_default)

                            setOnMarkerClickListener { _, _ ->
                                onMarkerTapped(place)
                                true  // consume event so onMapTapped is NOT also fired
                            }
                        }
                    }

                    // ensure marker exists in overlays
                    if (!mapView.overlays.contains(marker)) {
                        mapView.overlays.add(marker)
                    }

                    val isSelected = selectedPlace?.id == place.id
                    marker.alpha = if (isSelected) 1.0f else 0.65f

                    // Only update icon if selection changed
                    if (selectedPlace?.id != lastSelectedId) {

                        val baseIcon = baseMarkerIcon?.mutate()

                        if (isSelected) {
                            baseIcon?.setBounds(0, 0, 96, 144)
                        } else {
                            baseIcon?.setBounds(0, 0, 64, 96)
                        }

                        marker.icon = baseIcon
                    }
                }
                lastSelectedId = selectedPlace?.id

                // Auto zoom first load
                if (firstLoad) {

                    mapView.post {

                        when (geoPoints.size) {

                            0 -> {
                                // No pins → global view
                                mapView.controller.setZoom(3.0)
                                mapView.controller.setCenter(
                                    GeoPoint(20.0, 0.0)
                                )
                            }

                            1 -> {
                                // One pin → center nicely
                                mapView.controller.setZoom(14.0)
                                mapView.controller.setCenter(geoPoints.first())
                            }

                            else -> {
                                val boundingBox = BoundingBox.fromGeoPoints(geoPoints)

                                val padding = (mapView.width * 0.18).toInt()

                                mapView.zoomToBoundingBox(
                                    boundingBox,
                                    true,
                                    padding
                                )

                                // Shift camera upward to compensate for bottom sheet
                                mapView.post {
                                    mapView.scrollBy(0, -(mapView.height * 0.18).toInt())
                                }
                            }
                        }
                    }

                    firstLoad = false
                }
            }
        )

        // Zoom controls - Aligned to TopEnd to stay above the bottom sheet
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = { mapView.controller.zoomIn() },
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.85f),
                shadowElevation = 6.dp,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.Black)
                }
            }

            Surface(
                onClick = { mapView.controller.zoomOut() },
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.85f),
                shadowElevation = 6.dp,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.Black)
                }
            }
        }
    }
}