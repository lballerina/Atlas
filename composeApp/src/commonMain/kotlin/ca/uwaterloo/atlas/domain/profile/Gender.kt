package ca.uwaterloo.atlas.domain.profile

enum class Gender(val label: String) {
    MALE("Male"),
    FEMALE("Female"),
    NON_BINARY("Non-binary"),
    OTHER("Other"),
    PREFER_NOT_TO_SAY("Prefer not to say")
}
