package com.peco2282.devcore.adventure.builder

import com.peco2282.devcore.adventure.StyleDsl
import net.kyori.adventure.text.format.TextColor

@StyleDsl
interface Gradient {
  /**
   * グラデーションに使用する色を設定します。
   *
   * @param colors 色のリスト
   * @return この Gradient インスタンス
   */
  fun colors(vararg colors: TextColor): Gradient

  /**
   * グラデーションのフェーズ（開始位置のオフセット）を設定します。
   *
   * @param phase フェーズ (0.0 ~ 1.0)
   * @return この Gradient インスタンス
   */
  fun phase(phase: Float): Gradient

  /**
   * 各色の重み（その色がつく長さの比率）を設定します。
   * 指定しない場合は均等になります。
   *
   * @param weights 重みのリスト
   * @return この Gradient インスタンス
   */
  fun weights(vararg weights: Double): Gradient
}

internal class GradientImpl : Gradient {
  var colors: List<TextColor> = emptyList()
    private set
  var phase: Float = 0f
    private set
  var weights: List<Double> = emptyList()
    private set

  override fun colors(vararg colors: TextColor): Gradient = apply {
    this.colors = colors.toList()
  }

  override fun phase(phase: Float): Gradient = apply {
    this.phase = phase
  }

  override fun weights(vararg weights: Double): Gradient = apply {
    this.weights = weights.toList()
  }
}