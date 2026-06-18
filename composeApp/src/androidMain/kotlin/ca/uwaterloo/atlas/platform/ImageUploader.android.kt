package ca.uwaterloo.atlas.platform

import android.net.Uri
import ca.uwaterloo.atlas.SupabaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidImageUploader : ImageUploader {
    override suspend fun uploadImage(uri: String, bucket: String): String? {
        val androidUri = Uri.parse(uri)
        println("[AndroidImageUploader] Starting upload for URI: $uri to bucket: $bucket")
        return withContext(Dispatchers.IO) {
            try {
                // Check if we can open the stream
                val contentResolver = ContextProvider.context.contentResolver
                val inputStream = try {
                    contentResolver.openInputStream(androidUri)
                } catch (e: Exception) {
                    println("[AndroidImageUploader] CRITICAL: Could not open input stream for URI: $uri. Error: ${e.message}")
                    null
                }

                val bytes = inputStream?.use { it.readBytes() }
                
                if (bytes == null || bytes.isEmpty()) {
                    println("[AndroidImageUploader] FAILED: Bytes are null or empty for URI: $uri")
                    return@withContext null
                }
                
                println("[AndroidImageUploader] Read ${bytes.size} bytes. Sending to Supabase...")
                
                val fileName = "${System.currentTimeMillis()}_${androidUri.lastPathSegment ?: "image"}.jpg"
                
                // Wrap the Supabase call specifically to catch network/policy errors
                try {
                    val resultUrl = SupabaseStorage.uploadImage(bucket, fileName, bytes)
                    println("[AndroidImageUploader] SUCCESS! Public URL: $resultUrl")
                    resultUrl
                } catch (e: Exception) {
                    println("[AndroidImageUploader] SUPABASE STORAGE ERROR: ${e.message}")
                    // This is where a 403 (Policy error) or 400 (Bad request) would be caught
                    e.printStackTrace()
                    null
                }
            } catch (e: Exception) {
                println("[AndroidImageUploader] UNEXPECTED ERROR: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }
}

actual fun getImageUploader(): ImageUploader = AndroidImageUploader()
