package com.peco2282.devcore.scoreboard.nms

import com.peco2282.devcore.scoreboard.api.ScoreboardApi
import com.peco2282.devcore.scoreboard.api.factory.ScoreboardFactory

interface ScoreboardNMSFactory : ScoreboardFactory {
  companion object {
    fun get(): ScoreboardNMSFactory? = ScoreboardApi.findFactory()
  }
}