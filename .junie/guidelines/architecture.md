# Architecture and Multi-Module Structure

DevCore は、高レベルな API と、Paper/Minecraft のバージョン固有の低レベル実装を組み合わせたマルチモジュール構成を採用しています。

## 1. モジュール構成の原則

### 共通 API モジュール (例: `packet`, `gui`, `event`)
- `src/main/kotlin` にインターフェースや共通のビジネスロジック、DSL エントリポイントを配置する。
- Paper API (adventure など) への依存は最小限に抑え、NMS 直接の利用は `InternalAPI` オブジェクトやリフレクションを介してのみ行う。
- `build.gradle.kts` では、`io.papermc.paperweight.userdev` プラグインを使用し、開発バンドルを実装する。

### バージョン固有モジュール (例: `packet:v1_21_4`, `scoreboard-nms:v1_20_2`)
- バージョン名（例: `v1_21_4`）でサブプロジェクトを分割する。
- 各バージョン固有の NMS (Packet クラスなど) を直接操作し、共通 API で定義されたインターフェース（例: `FakeEntityBuilder`）を実装する。
- プロジェクトルートの `settings.gradle.kts` で正しく `include` されていることを確認する。

### コア・その他
- `core`: プラグイン全体の基盤となる共通機能を配置。
- `bom`: 依存関係のバージョン管理を一括で行う。
- `TestPlugin`: 開発中の機能を検証するためのランタイム・テスト用プラグイン。

## 2. 依存関係の管理
- `libs.versions.toml`（または `buildSrc` の依存管理）を利用し、プロジェクト全体でライブラリのバージョンを統一する。
- 新しいモジュールを追加する際は、`settings.gradle.kts` だけでなく、ルートの `build.gradle.kts` や関連するモジュールの `dependencies` ブロックに正しく定義する。

## 3. バージョン抽象化のフロー
1. `common` モジュールで、`interface` と `InternalAPI.someAction()` を定義。
2. `InternalAPI` は、実行環境のサーバーバージョンを検出し、適切なバージョン固有の実装を動的に読み込む（または静的に紐付ける）。
3. ユーザーは常に `common` モジュールの API を呼び出し、バージョンの違いを意識せずに済むように設計する。
