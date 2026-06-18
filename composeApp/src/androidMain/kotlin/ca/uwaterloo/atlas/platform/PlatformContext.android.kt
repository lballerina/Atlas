package ca.uwaterloo.atlas.platform

import android.content.Context

object ContextProvider {
    lateinit var context: Context
}

class AndroidPlatformContext : PlatformContext

actual fun getPlatformContext(): PlatformContext = AndroidPlatformContext()
