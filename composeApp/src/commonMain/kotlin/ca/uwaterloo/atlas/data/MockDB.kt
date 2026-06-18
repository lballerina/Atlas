package ca.uwaterloo.atlas.data

import atlas.composeapp.generated.resources.Res
import atlas.composeapp.generated.resources.paris
import atlas.composeapp.generated.resources.rome
import atlas.composeapp.generated.resources.tokyo
import ca.uwaterloo.atlas.domain.trip.TripData
import kotlinx.datetime.LocalDate
import ca.uwaterloo.atlas.domain.place.Place
import ca.uwaterloo.atlas.domain.place.PlaceCategory
import ca.uwaterloo.atlas.domain.place.CostLevel
import ca.uwaterloo.atlas.domain.place.TimeOfDay
import ca.uwaterloo.atlas.domain.profile.AgeRange
import ca.uwaterloo.atlas.domain.profile.Gender
import ca.uwaterloo.atlas.domain.profile.TravelStats
import ca.uwaterloo.atlas.domain.profile.TravelStyleTag
import ca.uwaterloo.atlas.domain.profile.UserProfile
import ca.uwaterloo.atlas.domain.credential.UserCredential

/**
 * Substitutes for a real database during Sprint 2.
 *
 * All hardcoded test data lives here so there is a single place to update
 * it, and so the Domain and UI layers never have literals scattered through
 * them. In Sprint 3 this object will be replaced by real API / database calls
 * without touching any Domain or UI code.
 */
object MockDB {

    val trips: List<TripData> = listOf(
        TripData(
            id = "1",
            title = "Summer in Paris",
            location = "Paris, France",
            imageUrl = "",
            drawableRes = Res.drawable.paris,
            startDate = LocalDate(2025, 7, 15),
            endDate = LocalDate(2025, 7, 22),
            placesCount = 8,
            isPublic = true,
            tags = listOf("Culture", "Foodie")
        ),
        TripData(
            id = "2",
            title = "Tokyo Adventures",
            location = "Tokyo, Japan",
            imageUrl = "",
            drawableRes = Res.drawable.tokyo,
            startDate = LocalDate(2025, 8, 1),
            endDate = LocalDate(2025, 8, 10),
            placesCount = 12,
            isPublic = false,
            tags = listOf("Adventure", "Solo")
        ),
        TripData(
            id = "3",
            title = "Weekend in New York",
            location = "New York, USA",
            imageUrl = "",
            drawableRes = Res.drawable.tokyo,
            startDate = LocalDate(2025, 9, 5),
            endDate = LocalDate(2025, 9, 7),
            placesCount = 5,
            isPublic = true,
            tags = listOf("Solo")
        ),
        TripData(
            id = "101",
            title = "Barcelona Food Tour",
            location = "Barcelona, Spain",
            imageUrl = "",
            drawableRes = Res.drawable.rome,
            startDate = LocalDate(2025, 6, 1),
            endDate = LocalDate(2025, 6, 7),
            isPublic = true,
            author = "Sofia Martinez",
            tags = listOf("Solo", "Foodie")
        ),
        TripData(
            id = "102",
            title = "NYC Weekend Getaway",
            location = "New York, USA",
            imageUrl = "",
            drawableRes = Res.drawable.paris,
            startDate = LocalDate(2025, 7, 11),
            endDate = LocalDate(2025, 7, 13),
            isPublic = true,
            author = "John Smith",
            tags = listOf("Solo")
        ),
        TripData(
            id = "103",
            title = "Iceland Road Trip",
            location = "Reykjavik, Iceland",
            imageUrl = "",
            drawableRes = Res.drawable.tokyo,
            startDate = LocalDate(2025, 8, 5),
            endDate = LocalDate(2025, 8, 20),
            isPublic = true,
            author = "Emma Johnson",
            tags = listOf("Adventure", "Nature")
        ),
        TripData(
            id = "104",
            title = "Rome Historical Tour",
            location = "Rome, Italy",
            imageUrl = "",
            drawableRes = Res.drawable.rome,
            startDate = LocalDate(2025, 9, 10),
            endDate = LocalDate(2025, 9, 17),
            isPublic = true,
            author = "Marco Rossi",
            tags = listOf("Culture", "History")
        ),
        TripData(
            id = "105",
            title = "Bali Relaxation",
            location = "Bali, Indonesia",
            imageUrl = "",
            drawableRes = Res.drawable.paris,
            startDate = LocalDate(2025, 10, 1),
            endDate = LocalDate(2025, 10, 14),
            isPublic = true,
            author = "Sarah Chen",
            tags = listOf("Beach", "Wellness")
        )
    )

    val places: List<Place> = listOf(
        Place(
            id = "place_1",
            tripId = "1",
            name = "Café de Flore",
            category = PlaceCategory.CAFE,
            latitude = 48.855,
            longitude = 2.333,
            address = "Paris, France",
            notes = "Amazing croissants and coffee.",
            dateVisited = LocalDate(2025, 7, 16),
            photos = listOf(
                "https://assets.newatlas.com/dims4/default/610b109/2147483647/strip/true/crop/6240x4160+0+0/resize/2880x1920!/format/webp/quality/90/?url=https%3A%2F%2Fnewatlas-brightspot.s3.amazonaws.com%2F1e%2Fb7%2F893a46b340b1b287f34fcadd05c1%2Fdepositphotos-307870382-xl.jpg",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSRWLhTB_9Q1sCO6MucLigJ5IohxZ3Og2MUXQ&s"
            ),
            photoCaptions = mapOf(
                "https://assets.newatlas.com/dims4/default/610b109/2147483647/strip/true/crop/6240x4160+0+0/resize/2880x1920!/format/webp/quality/90/?url=https%3A%2F%2Fnewatlas-brightspot.s3.amazonaws.com%2F1e%2Fb7%2F893a46b340b1b287f34fcadd05c1%2Fdepositphotos-307870382-xl.jpg" to "Morning coffee",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSRWLhTB_9Q1sCO6MucLigJ5IohxZ3Og2MUXQ&s" to "Outdoor seating"
            ),
            thumbnailPhoto = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSRWLhTB_9Q1sCO6MucLigJ5IohxZ3Og2MUXQ&s",
            rating = 4.5f,
            mood = "😊",
            tags = listOf("Cafe", "Romantic"),
            costIndicator = CostLevel.MODERATE,
            timeOfDay = TimeOfDay.MORNING,
            isFavorite = true
        ),
        Place(
            id = "place_2",
            tripId = "1",
            name = "Louvre Museum",
            category = PlaceCategory.MUSEUM,
            latitude = 48.8606,
            longitude = 2.3376,
            address = "Rue de Rivoli, Paris",
            notes = "Spent 3 hours here. Mona Lisa was packed!",
            photos = listOf("https://www.theartlifegallery.com/blog/wp-content/uploads/2023/08/Image-01-1.jpg",
                "https://www.parisinsidersguide.com/image-files/louvre-mona-lisa-crowds2-dreamstime-mc-mod-1000-2x1.jpg"),
            dateVisited = LocalDate(2025, 7, 16),
            rating = 5.0f,
            costIndicator = CostLevel.EXPENSIVE,
            timeOfDay = TimeOfDay.AFTERNOON,
            isFavorite = true
        ),
        Place(
            id = "place_3",
            tripId = "1",
            name = "Luxembourg Gardens",
            category = PlaceCategory.PARK,
            latitude = 48.8462,
            longitude = 2.3372,
            address = "Paris, France",
            notes = "Beautiful place to relax and journal.",
            photos = listOf("https://misadventureswithandi.com/wp-content/uploads/2022/02/Paris-6th-arrondissement-Jardin-du-Luxembourg-Palais-du-Luxembourg-8-1.jpg"),
            dateVisited = LocalDate(2025, 7, 17),
            rating = 4.2f,
            mood = "😌",
            costIndicator = CostLevel.BUDGET,
            timeOfDay = TimeOfDay.MORNING
        ),
        Place(
            id = "place_4",
            tripId = "2",
            name = "Shibuya Crossing",
            category = PlaceCategory.ATTRACTION,
            latitude = 35.6595,
            longitude = 139.7005,
            address = "Tokyo, Japan",
            notes = "So chaotic but iconic.",
            photos = listOf("https://cdn.cheapoguides.com/wp-content/uploads/sites/2/2019/07/scramble-crossing-shibuya-iStock-Nikada-1024x600.jpg"),
            dateVisited = LocalDate(2025, 8, 2),
            rating = 4.8f,
            mood = "🤩",
            costIndicator = CostLevel.BUDGET,
            timeOfDay = TimeOfDay.EVENING,
            isFavorite = true
        ),
        Place(
            id = "place_5",
            tripId = "2",
            name = "Tsukiji Outer Market",
            category = PlaceCategory.RESTAURANT,
            latitude = 35.6655,
            longitude = 139.7708,
            address = "Tokyo, Japan",
            notes = "Freshest sushi I've ever had.",
            photos = listOf("https://foodsaketokyo.com/wp-content/uploads/2011/07/tsukiji-sushi.jpg"),
            dateVisited = LocalDate(2025, 8, 3),
            rating = 4.7f,
            mood = "😋",
            costIndicator = CostLevel.MODERATE,
            timeOfDay = TimeOfDay.MORNING
        ),
        Place(
            id = "place_101_1",
            tripId = "101",
            name = "La Boqueria Market",
            category = PlaceCategory.RESTAURANT,
            latitude = 41.3825,
            longitude = 2.1721,
            address = "Barcelona, Spain",
            notes = "Incredible tapas and fresh juice stands.",
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/6/6e/La_Boqueria_market_Barcelona.jpg"
            ),
            thumbnailPhoto = "https://upload.wikimedia.org/wikipedia/commons/6/6e/La_Boqueria_market_Barcelona.jpg",
            dateVisited = LocalDate(2025, 6, 2),
            rating = 4.6f,
            costIndicator = CostLevel.MODERATE,
            timeOfDay = TimeOfDay.AFTERNOON,
            isFavorite = true
        ),
        Place(
            id = "place_101_2",
            tripId = "101",
            name = "Barceloneta Beach",
            category = PlaceCategory.PARK,
            latitude = 41.3780,
            longitude = 2.1925,
            address = "Barcelona, Spain",
            notes = "Perfect sunset spot.",
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/3/3c/Playa_de_la_Barceloneta.jpg"
            ),
            thumbnailPhoto = "https://upload.wikimedia.org/wikipedia/commons/3/3c/Playa_de_la_Barceloneta.jpg",
            dateVisited = LocalDate(2025, 6, 4),
            rating = 4.4f,
            costIndicator = CostLevel.BUDGET,
            timeOfDay = TimeOfDay.EVENING
        ),
        Place(
            id = "place_102_1",
            tripId = "102",
            name = "Central Park",
            category = PlaceCategory.PARK,
            latitude = 40.7851,
            longitude = -73.9683,
            address = "New York, USA",
            notes = "Morning jog and coffee walk.",
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/e/e6/Central_Park_New_York_City_New_York_23_crop.jpg"
            ),
            thumbnailPhoto = "https://upload.wikimedia.org/wikipedia/commons/e/e6/Central_Park_New_York_City_New_York_23_crop.jpg",
            dateVisited = LocalDate(2025, 7, 12),
            rating = 4.9f,
            timeOfDay = TimeOfDay.MORNING
        ),
        Place(
            id = "place_102_2",
            tripId = "102",
            name = "Brooklyn Bridge",
            category = PlaceCategory.ATTRACTION,
            latitude = 40.7061,
            longitude = -73.9969,
            address = "New York, USA",
            notes = "Great skyline views.",
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/0/00/Brooklyn_Bridge_Manhattan.jpg"
            ),
            thumbnailPhoto = "https://upload.wikimedia.org/wikipedia/commons/0/00/Brooklyn_Bridge_Manhattan.jpg",
            dateVisited = LocalDate(2025, 7, 12),
            rating = 4.8f,
            timeOfDay = TimeOfDay.EVENING
        ),
        Place(
            id = "place_103_1",
            tripId = "103",
            name = "Blue Lagoon",
            category = PlaceCategory.ATTRACTION,
            latitude = 63.8804,
            longitude = -22.4495,
            address = "Reykjavik, Iceland",
            notes = "Geothermal spa experience.",
            photos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/e/e6/Central_Park_New_York_City_New_York_23_crop.jpg"
            ),
            thumbnailPhoto = "https://upload.wikimedia.org/wikipedia/commons/e/e6/Central_Park_New_York_City_New_York_23_crop.jpg",
            dateVisited = LocalDate(2025, 8, 6),
            rating = 4.7f,
            costIndicator = CostLevel.EXPENSIVE,
            timeOfDay = TimeOfDay.AFTERNOON
        ),
        Place(
            id = "place_103_2",
            tripId = "103",
            name = "Skogafoss Waterfall",
            category = PlaceCategory.ATTRACTION,
            latitude = 63.5321,
            longitude = -19.5110,
            address = "Iceland",
            notes = "Huge waterfall with rainbows.",
            photos = listOf("https://upload.wikimedia.org/wikipedia/commons/8/8c/2008-05-24_35_Sk%C3%B3gafoss.jpg"),
            thumbnailPhoto = "https://upload.wikimedia.org/wikipedia/commons/8/8c/2008-05-24_35_Sk%C3%B3gafoss.jpg",
            dateVisited = LocalDate(2025, 8, 10),
            rating = 5.0f
        )
    )

    var currentUserProfile: UserProfile = UserProfile(
        displayName = "Andrew Anderson",
        email = "andrew.anderson@email.com",
        bio = "Food lover. Camera always in hand. Usually planning the next trip.",
        gender = Gender.MALE,
        ageRange = AgeRange.LATE_20S,
        tags = setOf(
            TravelStyleTag.SOLO,
            TravelStyleTag.FOODIE,
            TravelStyleTag.CULTURE,
            TravelStyleTag.PHOTOGRAPHY
        ),
        stats = TravelStats(trips = 12, places = 47, countries = 8),
        avatarUrl = ""
    )

    var allUsers: List<UserCredential> = listOf(UserCredential(
        email = "andrew.anderson@email.com",
        password = "1234"
    ))

    // Mutable so save/unsave mutations from TripModel are reflected here.
    val savedTripIds: MutableSet<String> = mutableSetOf("101", "103")

    val otherUsers: Map<String, UserProfile> = mapOf(
        "Sofia Martinez" to UserProfile(
            displayName = "Sofia Martinez",
            avatarUrl = "",
            email = "sofia@email.com",
            bio = "Food lover. Camera always in hand. Usually planning the next trip.",
            gender = Gender.FEMALE,
            ageRange = AgeRange.LATE_20S,
            tags = setOf(
                TravelStyleTag.SOLO,
                TravelStyleTag.FOODIE,
                TravelStyleTag.CULTURE,
                TravelStyleTag.PHOTOGRAPHY
            ),
            stats = TravelStats(trips = 9, places = 38, countries = 7)
        ),
        "John Smith" to UserProfile(
            displayName = "John Smith",
            avatarUrl = "",
            email = "john@email.com",
            bio = "Weekend getaways, city walks, and coffee runs.",
            gender = Gender.MALE,
            ageRange = AgeRange.EARLY_30S,
            tags = setOf(
                TravelStyleTag.SOLO,
                TravelStyleTag.CULTURE
            ),
            stats = TravelStats(trips = 6, places = 21, countries = 4)
        )
    )
}
