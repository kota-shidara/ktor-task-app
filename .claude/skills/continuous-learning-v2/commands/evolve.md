---
name: evolve
description: 関連インスティンクトをスキル、コマンド、またはエージェントにクラスタリング
command: /evolve
implementation: python3 ~/.claude/skills/continuous-learning-v2/scripts/instinct-cli.py evolve
---

# Evolveコマンド

## 実装

```bash
python3 ~/.claude/skills/continuous-learning-v2/scripts/instinct-cli.py evolve [--generate]
```

インスティンクトを分析し、関連するものを上位構造にクラスタリングします:
- **コマンド**: インスティンクトがユーザー起動のアクションを記述する場合
- **スキル**: インスティンクトが自動トリガーの動作を記述する場合
- **エージェント**: インスティンクトが複雑な複数ステップのプロセスを記述する場合

## 使い方

```
/evolve                    # すべてのインスティンクトを分析して進化を提案
/evolve --domain testing   # testingドメインのインスティンクトのみ進化
/evolve --dry-run          # 作成せずにプレビューを表示
/evolve --threshold 5      # クラスタに5個以上の関連インスティンクトを要求
```

## 進化ルール

### → コマンド（ユーザー起動）
インスティンクトがユーザーが明示的に要求するアクションを記述する場合:
- 「ユーザーが...を求めた時」に関する複数のインスティンクト
- 「新しいXを作成する時」のようなトリガーを持つインスティンクト
- 繰り返し可能なシーケンスに従うインスティンクト

例:
- `new-table-step1`: 「データベーステーブル追加時、マイグレーションを作成」
- `new-table-step2`: 「データベーステーブル追加時、スキーマを更新」
- `new-table-step3`: 「データベーステーブル追加時、型を再生成」

→ 作成: `/new-table` コマンド

### → スキル（自動トリガー）
インスティンクトが自動的に発生すべき動作を記述する場合:
- パターンマッチングトリガー
- エラーハンドリングレスポンス
- コードスタイル適用

例:
- `prefer-functional`: 「関数を書く時、関数型スタイルを優先」
- `use-immutable`: 「状態変更時、不変パターンを使用」
- `avoid-classes`: 「モジュール設計時、クラスベース設計を避ける」

→ 作成: `functional-patterns` スキル

### → エージェント（深さ/分離が必要）
インスティンクトが分離の恩恵を受ける複雑な複数ステップのプロセスを記述する場合:
- デバッグワークフロー
- リファクタリングシーケンス
- 調査タスク

例:
- `debug-step1`: 「デバッグ時、まずログを確認」
- `debug-step2`: 「デバッグ時、失敗するコンポーネントを特定」
- `debug-step3`: 「デバッグ時、最小再現を作成」
- `debug-step4`: 「デバッグ時、テストで修正を検証」

→ 作成: `debugger` エージェント

## 処理内容

1. `~/.claude/homunculus/instincts/`からすべてのインスティンクトを読み取り
2. インスティンクトをグループ化:
   - ドメインの類似性
   - トリガーパターンの重複
   - アクションシーケンスの関連性
3. 3個以上の関連インスティンクトのクラスタごとに:
   - 進化タイプを決定（コマンド/スキル/エージェント）
   - 適切なファイルを生成
   - `~/.claude/homunculus/evolved/{commands,skills,agents}/`に保存
4. 進化構造をソースインスティンクトにリンク

## 出力フォーマット

```
🧬 進化分析
==================

進化可能な3つのクラスタが見つかりました:

## クラスタ1: データベースマイグレーションワークフロー
インスティンクト: new-table-migration, update-schema, regenerate-types
タイプ: コマンド
信頼度: 85%（12回の観測に基づく）

作成予定: /new-table コマンド
ファイル:
  - ~/.claude/homunculus/evolved/commands/new-table.md

## クラスタ2: 関数型コードスタイル
インスティンクト: prefer-functional, use-immutable, avoid-classes, pure-functions
タイプ: スキル
信頼度: 78%（8回の観測に基づく）

作成予定: functional-patterns スキル
ファイル:
  - ~/.claude/homunculus/evolved/skills/functional-patterns.md

## クラスタ3: デバッグプロセス
インスティンクト: debug-check-logs, debug-isolate, debug-reproduce, debug-verify
タイプ: エージェント
信頼度: 72%（6回の観測に基づく）

作成予定: debugger エージェント
ファイル:
  - ~/.claude/homunculus/evolved/agents/debugger.md

---
`/evolve --execute` でこれらのファイルを作成します。
```

## フラグ

- `--execute`: 進化構造を実際に作成（デフォルトはプレビュー）
- `--dry-run`: 作成せずにプレビュー
- `--domain <name>`: 指定ドメインのインスティンクトのみ進化
- `--threshold <n>`: クラスタ形成に必要な最小インスティンクト数（デフォルト: 3）
- `--type <command|skill|agent>`: 指定タイプのみ作成

## 生成ファイルフォーマット

### コマンド
```markdown
---
name: new-table
description: Create a new database table with migration, schema update, and type generation
command: /new-table
evolved_from:
  - new-table-migration
  - update-schema
  - regenerate-types
---

# New Table Command

[クラスタ化されたインスティンクトに基づく生成コンテンツ]

## Steps
1. ...
2. ...
```

### スキル
```markdown
---
name: functional-patterns
description: Enforce functional programming patterns
evolved_from:
  - prefer-functional
  - use-immutable
  - avoid-classes
---

# Functional Patterns Skill

[クラスタ化されたインスティンクトに基づく生成コンテンツ]
```

### エージェント
```markdown
---
name: debugger
description: Systematic debugging agent
model: sonnet
evolved_from:
  - debug-check-logs
  - debug-isolate
  - debug-reproduce
---

# Debugger Agent

[クラスタ化されたインスティンクトに基づく生成コンテンツ]
```
