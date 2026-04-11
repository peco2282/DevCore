package com.peco2282.devcore.command.argument

import org.bukkit.Bukkit


object DevCoreArgumentTypes: DevCoreArgumentTypeProvider by DevCoreArgumentTypes.getProvider() {

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