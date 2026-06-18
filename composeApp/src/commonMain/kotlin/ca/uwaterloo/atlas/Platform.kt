package ca.uwaterloo.atlas

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform