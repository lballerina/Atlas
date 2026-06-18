package ca.uwaterloo.atlas.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.atlas.domain.trip.TripData
import ca.uwaterloo.atlas.platform.PlatformImage
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripGrid(
    trips: List<TripData>,
    modifier: Modifier = Modifier,
    onTripClick: (TripData) -> Unit = {}
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        trips.forEach { trip ->
            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                TripGridCard(
                    trip = trip,
                    onClick = { onTripClick(trip) }
                )
            }
        }
    }
}

@Composable
fun TripGridCard(
    trip: TripData,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .shadow(4.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = Color.White
    ) {
        Column(Modifier.fillMaxSize()) {
            TripCoverImage(
                trip = trip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = trip.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = trip.location,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Renders the cover image for a trip using the best available source:
 *
 * 1. [TripData.imageUrl] is non-empty → user-picked photo (local URI or
 *    remote URL). Rendered by [PlatformImage] which handles both on Android
 *    and iOS via Coil / Kingfisher.
 * 2. [TripData.drawableRes] is non-null → bundled drawable fallback used by
 *    MockDB entries. Rendered by the standard Compose [Image].
 * 3. Neither → grey placeholder box.
 *
 * This composable is shared by [TripGridCard] and [TripCard] (My Trips list)
 * so both surfaces update automatically when a cover photo is set.
 */
@Composable
fun TripCoverImage(
    trip: TripData,
    modifier: Modifier = Modifier
) {
    when {
        trip.imageUrl.isNotEmpty() -> {
            PlatformImage(
                uri = trip.imageUrl,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
        trip.drawableRes != null -> {
            Image(
                painter = painterResource(trip.drawableRes),
                contentDescription = trip.title,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
        else -> {
            Box(
                modifier = modifier.background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Text("🗺️", fontSize = 28.sp)
            }
        }
    }
}
