package ca.uwaterloo.atlas.platform

class JvmPlatformContext : PlatformContext

actual fun getPlatformContext(): PlatformContext = JvmPlatformContext()
