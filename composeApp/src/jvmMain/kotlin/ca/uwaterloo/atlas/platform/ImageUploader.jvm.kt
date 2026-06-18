package ca.uwaterloo.atlas.platform

class JvmImageUploader : ImageUploader {
    override suspend fun uploadImage(uri: String, bucket: String): String? {
        // Basic implementation for JVM (can be expanded if needed)
        return uri
    }
}

actual fun getImageUploader(): ImageUploader = JvmImageUploader()
