package ca.uwaterloo.atlas

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://ulcxyvywffoxuafjjszo.supabase.co",
        supabaseKey = "sb_publishable_w-r3kvCAV4vrpP-TtZynfw_0KUxVr71"
    ) {
        install(Postgrest)
        install(Auth)
        install(Storage)
    }
}