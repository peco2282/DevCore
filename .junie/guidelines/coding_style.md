# Coding Style Guidelines

このプロジェクトでは、Kotlin を主言語として使用し、読みやすく保守性の高いコードを目指します。

## 1. ネーミング規則
- **クラス名**: PascalCase (例: `PacketEvent`, `NetworkSettingsImpl`)
- **メソッド・変数名**: camelCase (例: `sendPacket`, `onPacketAsync`)
- **インターフェース**: 明確な機能名にする。実装クラスには `Impl` 接尾辞を付ける (例: `PacketListener` -> `PacketListenerImpl`)
- **DSL Builder**: `Builder` 接尾辞を付ける (例: `TitleBuilder`, `SoundBuilder`)

## 2. DSL の設計
- `@DslMarker` を使用したアノテーション（例: `@PacketDsl`）を定義し、Builder クラスに適用すること。
- 拡張関数を利用して、既存のクラス（特に `Player`）に DSL エントリポイントを提供すること。
  ```kotlin
  fun Player.packet(action: PacketBuilder.() -> Unit) { ... }
  ```

## 3. NMS (net.minecraft.server) の扱い
- NMS コードは可能な限り、バージョン固有のサブモジュール（例: `packet:v1_21_4`）に隔離すること。
- 共通 API モジュールからは `InternalAPI` オブジェクトやインターフェースを介して呼び出す。
- リフレクションを使用する場合は、`PacketWrapper.kt` にある `getFieldValue` / `setFieldValue` などのキャッシュ付きユーティリティを使用すること。

## 4. 非同期処理
- コルーチン（`kotlinx-coroutines`）を積極的に使用する。
- Bukkit API を呼び出す際は、スレッドセーフかどうかに注意し、必要に応じて同期・非同期を切り替える。
- `onPacketAsync` のように、suspend 関数を受け取るハンドラーを提供することを推奨する。

## 5. コメントとドキュメント
- 複雑なロジックや Public な API には KDoc を記述する。
- 特に NMS のバージョン依存箇所については、どのバージョンを想定しているか、または将来の変更可能性について注釈を入れること。
