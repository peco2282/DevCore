package com.peco2282.devcore.scoreboard.api.factory

import com.peco2282.devcore.scoreboard.api.ScoreboardApi

/**
 * A specialized factory interface for NMS-based scoreboard implementations.
 */
interface ScoreboardNMSFactory : ScoreboardFactory {
  companion object {
    /**
     * Finds the current factory instance if it's an NMS factory.
     */
    fun get(): ScoreboardNMSFactory? = try {
      Class.forName("com.peco2282.devcore.scoreboard.nms.NMSProvider")
      ScoreboardApi.findFactory()
    } catch (e: ClassNotFoundException) {

      null
    }
  }
}
