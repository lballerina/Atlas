package ca.uwaterloo.atlas

import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload

object SupabaseStorage {
    private val storage = SupabaseClient.client.storage

    suspend fun uploadImage(bucket: String, path: String, bytes: ByteArray): String {
        println("[SupabaseStorage] Uploading to bucket=$bucket, path=$path, size=${bytes.size} bytes")
        val bucketApi = storage.from(bucket)
        try {
            bucketApi.upload(path, bytes) {
                upsert = true
            }
            val publicUrl = bucketApi.publicUrl(path)
            println("[SupabaseStorage] Upload success! Public URL: $publicUrl")
            return publicUrl
        } catch (e: Exception) {
            println("[SupabaseStorage] Upload FAILED: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    fun getPublicUrl(bucket: String, path: String): String {
        return storage.from(bucket).publicUrl(path)
    }
}
