package com.peco2282.devcore.command.argument

import org.bukkit.Bukkit

/**
 * The central entry point for all DevCore argument types.
 *
 * `DevCoreArgumentTypes` implements [DevCoreArgumentTypeProvider] by delegating to a
 * version-specific implementation that is resolved at runtime based on the running
 * Minecraft server version. This allows command argument types that depend on NMS
 * (net.minecraft.server) internals to be used transparently across supported versions.
 *
 * ### Supported versions
 * - `1.20.x` → `com.peco2282.devcore.command.v1_20_6.DevCoreArgumentTypeProviderImpl`
 * - `1.21+`  → `com.peco2282.devcore.command.v1_21_1.DevCoreArgumentTypePrividerImpl`
 *
 * ### Usage
 *
 * Use `DevCoreArgumentTypes` directly inside a [CommandCreator] DSL block to obtain
 * argument types that are not available through the standard DSL helpers:
 *
 * ```kotlin
 * plugin.command("teleport") {
 *   argument("pos", DevCoreArgumentTypes.finePosition(true)) {
 *     executes { context ->
 *       val resolver = context.getArgument("pos", FinePositionResolver::class.java)
 *       val pos = resolver.resolve(context.source)
 *       (context.source.sender as? Player)?.teleport(pos.toLocation(world))
 *       1
 *     }
 *   }
 * }
 * ```
 *
 * You can also use it outside the DSL to obtain a raw [com.mojang.brigadier.arguments.ArgumentType]:
 *
 * ```kotlin
 * val rotationType = DevCoreArgumentTypes.rotation()
 * val teamType     = DevCoreArgumentTypes.team()
 * ```
 *
 * @see DevCoreArgumentTypeProvider for the full list of available argument types.
 */
object DevCoreArgumentTypes: DevCoreArgumentTypeProvider by DevCoreArgumentTypes.getProvider() {

  /**
   * Resolves and instantiates the version-specific [DevCoreArgumentTypeProvider] implementation.
   *
   * The implementation class is chosen based on the running Minecraft version string returned
   * by [Bukkit.getMinecraftVersion]. The class is loaded reflectively so that the common API
   * module does not need a compile-time dependency on any version-specific module.
   *
   * @return the [DevCoreArgumentTypeProvider] for the current server version
   * @throws RuntimeException if the provider class cannot be found or instantiated
   */
  internal fun getProvider(): DevCoreArgumentTypeProvider {
    val version = Bukkit.getMinecraftVersion()
    val className = if (version.startsWith("1.20")) {
      "com.peco2282.devcore.command.v1_20_6.DevCoreArgumentTypeProviderImpl"
    } else {
      "com.peco2282.devcore.command.v1_21_1.DevCoreArgumentTypePrividerImpl"
    }

    return try {
      Class.forName(className)
        .getDeclaredConstructor()
        .newInstance() as DevCoreArgumentTypeProvider
    } catch (e: Exception) {
      throw RuntimeException("Failed to load DevCoreArgumentTypeProvider for version $version", e)
    }
  }
}
