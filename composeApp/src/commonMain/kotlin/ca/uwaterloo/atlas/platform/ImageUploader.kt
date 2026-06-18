package ca.uwaterloo.atlas.platform

interface ImageUploader {
    suspend fun uploadImage(uri: String, bucket: String = "photos"): String?
}

expect fun getImageUploader(): ImageUploader
