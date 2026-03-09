package com.peco2282.testplugin

import com.peco2282.devcore.config.validations.annotations.*
import net.kyori.adventure.text.Component

enum class TestEnum {
  FIRST, SECOND, THIRD
}

@Comment("サブ設定のデータクラス")
data class SubConfig(
  @Comment("サブ設定の有効無効")
  val subEnabled: Boolean = false,
  @Comment("サブ設定のカウント")
  @Range(min = 0, max = 100)
  val count: Int = 0
)

@Comment("報酬の設定")
data class Reward(
  @Comment("アイテム名")
  @NotBlank
  val item: String = "DIAMOND",
  @Comment("数量")
  @Range(min = 1, max = 64)
  val amount: Int = 1
)

@Comment("カテゴリー設定")
data class Category(
  @Comment("カテゴリー名")
  @NotBlank
  val name: String = "Default",
  @Comment("報酬リスト")
  @Size(min = 1)
  val rewards: List<Reward> = listOf(Reward())
)

@Comment("メイン設定")
data class Config(
  @Comment("プラグインが有効かどうか")
  val enabled: Boolean = true,

  @Comment("送信されるメッセージ")
  @NotBlank
  val message: String = "Hello, DevCore Config!",

  @Comment("リッチテキストメッセージ (MiniMessage形式)")
  val formattedMessage: Component = Component.text("<green>Hello <yellow>DevCore <red>Config!"),

  @Comment("列挙型の設定")
  val type: TestEnum = TestEnum.FIRST,

  @Comment("ネストされた設定")
  val sub: SubConfig = SubConfig(),

  @Comment("文字列のリスト")
  @Size(min = 1)
  val list: List<String> = listOf("apple", "banana", "orange"),

  @Comment("データクラスのリスト")
  val subList: List<SubConfig> = listOf(SubConfig(count = 1), SubConfig(count = 2)),

  @Comment("マップの設定")
  val map: Map<String, Int> = mapOf("one" to 1, "two" to 2),

  @Comment("データクラスのマップ")
  val subMap: Map<String, SubConfig> = mapOf("a" to SubConfig(count = 10), "b" to SubConfig(count = 20)),

  @Comment("複雑なネスト: カテゴリー別の報酬リスト")
  val categories: Map<String, Category> = mapOf(
    "common" to Category("Common Category", listOf(Reward("IRON_INGOT", 5))),
    "rare" to Category("Rare Category", listOf(Reward("GOLD_INGOT", 3), Reward("DIAMOND", 1)))
  ),

  @Comment("複雑なコレクション: マップの中にリスト")
  val tags: Map<String, List<String>> = mapOf(
    "group1" to listOf("tagA", "tagB"),
    "group2" to listOf("tagC")
  )
)


