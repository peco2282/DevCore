# entity

エンティティのスポーン、AI制御、ライフサイクル管理を簡単に行うためのモジュールです。

## 使用方法

### エンティティのスポーン
Kotlin DSL を使用して、型安全にエンティティをスポーンできます。

```kotlin
val zombie = location.spawn<Zombie> {
    isNoAi = true
    isCustomNameVisible = true
    customName = Component.text("カスタムゾンビ")
}
```

### AI制御
MobのAIやゴールの管理に便利なメソッドを提供します。

```kotlin
mob.targetNearestPlayer(10.0)
mob.moveTo(targetLocation, 1.2)
mob.clearGoals()
mob.addGoals(1, myCustomGoal)
```

### ライフサイクル管理
定期的なアクションや遅延削除など、エンティティに関する一般的なタスクを自動化します。

```kotlin
zombie.onTick(plugin, 20.ticks) {
    // エンティティが有効な間、1秒ごとに実行
    world.spawnParticle(Particle.HAPPY_VILLAGER, location, 5)
}

zombie.onDeath(plugin) {
    player.sendMessage("ゾンビが死亡しました！")
}

zombie.removeAfter(plugin, 60.seconds)
```

## 特徴
- **直感的なスポーンAPI**: 単一のブロック内でエンティティの生成と設定が可能。
- **AI拡張**: パスファインディング、ターゲット設定、ゴール管理を簡素化。
- **ライフサイクルヘルパー**: エンティティに紐付いたタスクやイベントリスナーを簡単に登録でき、自動クリーンアップにも対応。
- **Paper API サポート**: Paper の MobGoal API と完全に統合。
