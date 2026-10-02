# DevCore Database API

[[English](README.md)] | [日本語]

トランザクション、リポジトリ、ページネーション、スキーマ登録、順序付きマイグレーションを提供する、Exposedベースの共通APIです。

```kotlin
dependencies { implementation("com.peco2282.devcore:database-api:<version>") }
```

プロバイダー実装時に利用します。通常のアプリケーションでは`database-hikari`や`database-sqlite`などの具象アダプターを選択してください。
