package com.peco2282.devcore.effect

import org.bukkit.Location
import org.bukkit.Particle

/**
 * Utility for particle effects.
 */
object Effects {
  /**
   * Spawn a cloud of particles at the given location.
   */
  fun spawnCloud(location: Location, particle: Particle = Particle.CLOUD, count: Int = 10) {
    location.world?.spawnParticle(particle, location, count, 0.2, 0.2, 0.2, 0.05)
  }
}
