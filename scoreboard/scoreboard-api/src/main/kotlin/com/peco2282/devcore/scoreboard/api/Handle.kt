package com.peco2282.devcore.scoreboard.api

import org.bukkit.entity.Player

interface Handle {
  /**
   * スコアボード/ボスバーの状態を更新する
   */
  fun update()

  /**
   * プレイヤーに表示する
   */
  fun show(player: Player)

  /**
   * プレイヤーから非表示にする
   */
  fun hide(player: Player)

  /**
   * 全てのプレイヤーから非表示にし、リソースを解放する
   */
  fun destroy()
}

interface SidebarHandle : Handle
interface BossBarHandle : Handle
