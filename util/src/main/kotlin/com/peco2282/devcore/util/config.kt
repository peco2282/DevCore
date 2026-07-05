package com.peco2282.devcore.util

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.Vector


/**
 * Retrieves a Location from the configuration section at the specified path.
 *
 * The configuration is expected to have the following structure:
 * - `path.world`: String representing the world name
 * - `path.x`: Double representing the x-coordinate
 * - `path.y`: Double representing the y-coordinate
 * - `path.z`: Double representing the z-coordinate
 *
 * @param path The configuration path to read the location from
 * @param def The default location to return if the world is not found or the path is invalid. Defaults to null.
 * @return The Location object if the world exists and all coordinates are present, otherwise the default value
 */
fun ConfigurationSection.getAsLocation(path: String, def: Location? = null): Location? {
  val world = getString("$path.world") ?: return def
  val x = getDouble("$path.x")
  val y = getDouble("$path.y")
  val z = getDouble("$path.z")
  return Bukkit.getWorld(world)?.let { Location(it, x, y, z) } ?: def
}

/**
 * Retrieves a Vector from the configuration section at the specified path.
 *
 * The configuration is expected to have the following structure:
 * - `path.x`: Double representing the x-component
 * - `path.y`: Double representing the y-component
 * - `path.z`: Double representing the z-component
 *
 * @param path The configuration path to read the vector from
 * @return The Vector object constructed from the x, y, and z coordinates
 */
fun ConfigurationSection.getAsVector(path: String): Vector {
  val x = getDouble("$path.x")
  val y = getDouble("$path.y")
  val z = getDouble("$path.z")
  return Vector(x, y, z)
}
