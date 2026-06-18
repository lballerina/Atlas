package ca.uwaterloo.atlas.ui.utils

import ca.uwaterloo.atlas.ui.components.PlaceSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL
import java.net.HttpURLConnection
import java.net.URLEncoder

@Serializable
data class NominatimResponse(
    val display_name: String,
    val lat: String,
    val lon: String
)

suspend fun searchPlaces(query: String): List<PlaceSearchResult> {

    if (query.length < 3) return emptyList()

    return try {

        withContext(Dispatchers.IO) {

            val encodedQuery = URLEncoder.encode(query, "UTF-8")

            val url =
                "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=5"

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "AtlasApp")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val response = connection.inputStream
                .bufferedReader()
                .readText()

            val results =
                Json { ignoreUnknownKeys = true }
                    .decodeFromString<List<NominatimResponse>>(response)

            results.map {

                PlaceSearchResult(
                    name = it.display_name.split(",")[0],
                    address = it.display_name,
                    latitude = it.lat.toDouble(),
                    longitude = it.lon.toDouble()
                )
            }
        }

    } catch (e: Exception) {

        println("Place search failed: ${e.message}")

        emptyList()
    }
}