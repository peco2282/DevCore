# DevCore

Core library for the DevCore project.

## Requirements
- JDK 17+

## Build
```bash
./gradlew build
```

## Publish
Set either Gradle properties or environment variables, then run `./gradlew publish`.

- Gradle properties: `devcore.publish.releaseUrl`, `devcore.publish.snapshotUrl`, `devcore.publish.user`, `devcore.publish.password`
- Environment variables: `DEVCORE_PUBLISH_RELEASE_URL`, `DEVCORE_PUBLISH_SNAPSHOT_URL`, `DEVCORE_PUBLISH_USER`, `DEVCORE_PUBLISH_PASSWORD`

## Usage (Gradle)
Consume the unified dependency:

```kotlin
dependencies {
  implementation("com.peco2282.devcore:core:<version>")
}
```

Or use the BOM and pick modules:

```kotlin
dependencies {
  implementation(platform("com.peco2282.devcore:bom:<version>"))
  implementation("com.peco2282.devcore:command")
  implementation("com.peco2282.devcore:config")
}
```

## モジュール一覧

各モジュールは個別に導入することも、`core` を通じて一括で導入することも可能です。

- [**adventure**](adventure/README.md): AdventureライブラリをKotlinから使いやすくするためのDSL。直感的なテキスト構築とスタイリングが可能です。
- [**command**](command/README.md): Paper (Brigadier) コマンドを型安全なDSLで定義。引数定義や権限設定、サジェスチョンを簡潔に記述できます。
- [**config**](config/README.md): YAML設定をKotlinデータクラスへ自動マッピング。アノテーションによるバリデーションとコメントの自動挿入をサポートします。
- [**scheduler**](scheduler/README.md): Bukkit schedulerの薄いラッパー。Tickベースの時間指定や、プレイヤー/ワールドのライフサイクルに紐付いたタスク管理を提供します。
- [**cooldown**](cooldown/README.md): プレイヤーやシステム全般のクールダウンおよびデバウンス（連打防止）を管理するための汎用ユーティリティ。

## License
Apache License 2.0. See `LICENSE`.
