---
name: evolve
description: 関連するインスティンクトをスキル、コマンド、またはエージェントにクラスタリングする
command: /evolve
implementation: python3 ~/.claude/skills/continuous-learning-v2/scripts/instinct-cli.py evolve
---

# Evolve コマンド

## 実装

```bash
python3 ~/.claude/skills/continuous-learning-v2/scripts/instinct-cli.py evolve [--generate]
```

インスティンクトを分析し、関連するものをより高レベルの構造にクラスタリングする:
- **コマンド**: インスティンクトがユーザー起動のアクションを記述している場合
- **スキル**: インスティンクトが自動トリガーされる動作を記述している場合
- **エージェント**: インスティンクトが複雑な複数ステップのプロセスを記述している場合

## 使い方

```
/evolve                    # 全インスティンクトを分析し進化を提案
/evolve --domain testing   # testing ドメインのインスティンクトのみ進化
/evolve --dry-run          # 作成せずに結果をプレビュー
/evolve --threshold 5      # クラスタ形成に5つ以上の関連インスティンクトを要求
```

## 進化ルール

### → コマンド (ユーザー起動)
インスティンクトがユーザーが明示的に要求するアクションを記述している場合:
- 「ユーザーが〜を要求したとき」に関する複数のインスティンクト
- 「新しいXを作成するとき」のようなトリガーを持つインスティンクト
- 再現可能なシーケンスに従うインスティンクト

例:
- `new-table-step1`: "データベーステーブルを追加するとき、マイグレーションを作成"
- `new-table-step2`: "データベーステーブルを追加するとき、スキーマを更新"
- `new-table-step3`: "データベーステーブルを追加するとき、型を再生成"

→ 作成されるもの: `/new-table` コマンド

### → スキル (自動トリガー)
インスティンクトが自動的に実行されるべき動作を記述している場合:
- パターンマッチングによるトリガー
- エラーハンドリングの応答
- コードスタイルの強制

例:
- `prefer-functional`: "関数を書くとき、関数型スタイルを優先"
- `use-immutable`: "状態を変更するとき、イミュータブルパターンを使用"
- `avoid-classes`: "モジュールを設計するとき、クラスベースの設計を避ける"

→ 作成されるもの: `functional-patterns` スキル

### → エージェント (深さ/分離が必要)
インスティンクトが分離の恩恵を受ける複雑な複数ステップのプロセスを記述している場合:
- デバッグワークフロー
- リファクタリングシーケンス
- 調査タスク

例:
- `debug-step1`: "デバッグ時、まずログを確認"
- `debug-step2`: "デバッグ時、障害コンポーネントを分離"
- `debug-step3`: "デバッグ時、最小再現を作成"
- `debug-step4`: "デバッグ時、テストで修正を検証"

→ 作成されるもの: `debugger` エージェント

## 実行内容

1. `~/.claude/homunculus/instincts/` から全インスティンクトを読み込む
2. インスティンクトを以下でグループ化:
   - ドメインの類似性
   - トリガーパターンの重複
   - アクションシーケンスの関連性
3. 3つ以上の関連インスティンクトの各クラスタに対して:
   - 進化タイプ (コマンド/スキル/エージェント) を決定
   - 適切なファイルを生成
   - `~/.claude/homunculus/evolved/{commands,skills,agents}/` に保存
4. 進化した構造をソースインスティンクトにリンク

## 出力フォーマット

```
🧬 Evolve Analysis
==================

Found 3 clusters ready for evolution:

## Cluster 1: Database Migration Workflow
Instincts: new-table-migration, update-schema, regenerate-types
Type: Command
Confidence: 85% (based on 12 observations)

Would create: /new-table command
Files:
  - ~/.claude/homunculus/evolved/commands/new-table.md

## Cluster 2: Functional Code Style
Instincts: prefer-functional, use-immutable, avoid-classes, pure-functions
Type: Skill
Confidence: 78% (based on 8 observations)

Would create: functional-patterns skill
Files:
  - ~/.claude/homunculus/evolved/skills/functional-patterns.md

## Cluster 3: Debugging Process
Instincts: debug-check-logs, debug-isolate, debug-reproduce, debug-verify
Type: Agent
Confidence: 72% (based on 6 observations)

Would create: debugger agent
Files:
  - ~/.claude/homunculus/evolved/agents/debugger.md

---
Run `/evolve --execute` to create these files.
```

## フラグ

- `--execute`: 進化した構造を実際に作成 (デフォルトはプレビュー)
- `--dry-run`: 作成せずにプレビュー
- `--domain <name>`: 指定ドメインのインスティンクトのみ進化
- `--threshold <n>`: クラスタ形成に必要な最小インスティンクト数 (デフォルト: 3)
- `--type <command|skill|agent>`: 指定タイプのみ作成

## 生成ファイルのフォーマット

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

[Generated content based on clustered instincts]

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

[Generated content based on clustered instincts]
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

[Generated content based on clustered instincts]
```
