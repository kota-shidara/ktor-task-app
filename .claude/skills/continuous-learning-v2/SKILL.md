---
name: continuous-learning-v2
description: フックによるセッション観測、信頼度スコアリング付きアトミックインスティンクトの作成、スキル/コマンド/エージェントへの進化を行うインスティンクトベースの学習システム。
version: 2.0.0
---

# 継続学習 v2 - インスティンクトベースアーキテクチャ

Claude Codeセッションを再利用可能な知識に変換する高度な学習システム。信頼度スコアリング付きの小さな学習済み行動「インスティンクト」を基本単位とします。

## v2の新機能

| 機能 | v1 | v2 |
|---------|----|----|
| 観測 | Stopフック（セッション終了時） | PreToolUse/PostToolUse（100%信頼性） |
| 分析 | メインコンテキスト | バックグラウンドエージェント（Haiku） |
| 粒度 | 完全なスキル | アトミック「インスティンクト」 |
| 信頼度 | なし | 0.3-0.9の重み付き |
| 進化 | 直接スキル化 | インスティンクト → クラスタ → スキル/コマンド/エージェント |
| 共有 | なし | インスティンクトのエクスポート/インポート |

## インスティンクトモデル

インスティンクトは小さな学習済み行動です:

```yaml
---
id: prefer-functional-style
trigger: "when writing new functions"
confidence: 0.7
domain: "code-style"
source: "session-observation"
---

# Prefer Functional Style

## Action
Use functional patterns over classes when appropriate.

## Evidence
- Observed 5 instances of functional pattern preference
- User corrected class-based approach to functional on 2025-01-15
```

**特性:**
- **アトミック** -- 1つのトリガー、1つのアクション
- **信頼度加重** -- 0.3 = 暫定、0.9 = ほぼ確実
- **ドメインタグ付き** -- code-style、testing、git、debugging、workflowなど
- **エビデンス付き** -- 作成元の観察を追跡

## 仕組み

```
セッションアクティビティ
      │
      │ フックがプロンプト + ツール使用をキャプチャ（100%信頼性）
      ▼
┌─────────────────────────────────────────┐
│         observations.jsonl              │
│   （プロンプト、ツール呼び出し、結果）      │
└─────────────────────────────────────────┘
      │
      │ Observerエージェントが読み取り（バックグラウンド、Haiku）
      ▼
┌─────────────────────────────────────────┐
│          パターン検出                     │
│   • ユーザーの修正 → インスティンクト       │
│   • エラー解決 → インスティンクト          │
│   • 繰り返しワークフロー → インスティンクト  │
└─────────────────────────────────────────┘
      │
      │ 作成/更新
      ▼
┌─────────────────────────────────────────┐
│         instincts/personal/             │
│   • prefer-functional.md (0.7)          │
│   • always-test-first.md (0.9)          │
│   • use-zod-validation.md (0.6)         │
└─────────────────────────────────────────┘
      │
      │ /evolve がクラスタリング
      ▼
┌─────────────────────────────────────────┐
│              evolved/                   │
│   • commands/new-feature.md             │
│   • skills/testing-workflow.md          │
│   • agents/refactor-specialist.md       │
└─────────────────────────────────────────┘
```

## クイックスタート

### 1. 観測フックの有効化

`~/.claude/settings.json` に追加:

```json
{
  "hooks": {
    "PreToolUse": [{
      "matcher": "*",
      "hooks": [{
        "type": "command",
        "command": "~/.claude/skills/continuous-learning-v2/hooks/observe.sh pre"
      }]
    }],
    "PostToolUse": [{
      "matcher": "*",
      "hooks": [{
        "type": "command",
        "command": "~/.claude/skills/continuous-learning-v2/hooks/observe.sh post"
      }]
    }]
  }
}
```

### 2. ディレクトリ構造の初期化

```bash
mkdir -p ~/.claude/homunculus/{instincts/{personal,inherited},evolved/{agents,skills,commands}}
touch ~/.claude/homunculus/observations.jsonl
```

### 3. Observerエージェントの実行（オプション）

Observerをバックグラウンドで実行し、観測を分析できます:

```bash
# バックグラウンドObserverの起動
~/.claude/skills/continuous-learning-v2/agents/start-observer.sh
```

## コマンド

| コマンド | 説明 |
|---------|-------------|
| `/instinct-status` | すべての学習済みインスティンクトと信頼度を表示 |
| `/evolve` | 関連インスティンクトをスキル/コマンドにクラスタリング |
| `/instinct-export` | 共有用にインスティンクトをエクスポート |
| `/instinct-import <file>` | 他者のインスティンクトをインポート |

## 設定

`config.json` を編集:

```json
{
  "version": "2.0",
  "observation": {
    "enabled": true,
    "store_path": "~/.claude/homunculus/observations.jsonl",
    "max_file_size_mb": 10,
    "archive_after_days": 7
  },
  "instincts": {
    "personal_path": "~/.claude/homunculus/instincts/personal/",
    "inherited_path": "~/.claude/homunculus/instincts/inherited/",
    "min_confidence": 0.3,
    "auto_approve_threshold": 0.7,
    "confidence_decay_rate": 0.05
  },
  "observer": {
    "enabled": true,
    "model": "haiku",
    "run_interval_minutes": 5,
    "patterns_to_detect": [
      "user_corrections",
      "error_resolutions",
      "repeated_workflows",
      "tool_preferences"
    ]
  },
  "evolution": {
    "cluster_threshold": 3,
    "evolved_path": "~/.claude/homunculus/evolved/"
  }
}
```

## ファイル構造

```
~/.claude/homunculus/
├── identity.json           # プロフィール、技術レベル
├── observations.jsonl      # 現在のセッション観測
├── observations.archive/   # 処理済み観測
├── instincts/
│   ├── personal/           # 自動学習インスティンクト
│   └── inherited/          # 他者からインポートしたもの
└── evolved/
    ├── agents/             # 生成されたスペシャリストエージェント
    ├── skills/             # 生成されたスキル
    └── commands/           # 生成されたコマンド
```

## Skill Creatorとの連携

[Skill Creator GitHub App](https://skill-creator.app)を使用すると、**両方**が生成されます:
- 従来のSKILL.mdファイル（後方互換性のため）
- インスティンクトコレクション（v2学習システム用）

リポジトリ分析からのインスティンクトは`source: "repo-analysis"`を持ち、ソースリポジトリURLが含まれます。

## 信頼度スコアリング

信頼度は時間とともに変化します:

| スコア | 意味 | 動作 |
|-------|---------|----------|
| 0.3 | 暫定的 | 提案されるが強制されない |
| 0.5 | 中程度 | 関連する場合に適用 |
| 0.7 | 強い | 自動承認で適用 |
| 0.9 | ほぼ確実 | コア動作 |

**信頼度が上昇する場合:**
- パターンが繰り返し観察される
- ユーザーが提案された動作を修正しない
- 他ソースからの類似インスティンクトが一致

**信頼度が低下する場合:**
- ユーザーが明示的に動作を修正する
- 長期間パターンが観察されない
- 矛盾するエビデンスが現れる

## なぜ観測にスキルではなくフックを使うのか？

> 「v1はスキルに依存して観測していました。スキルは確率的で、Claudeの判断に基づき50-80%の確率でしか発火しません。」

フックは**100%の確率**で決定論的に発火します。これにより:
- すべてのツール呼び出しが観測される
- パターンの見逃しがない
- 学習が包括的になる

## 後方互換性

v2はv1と完全に互換性があります:
- 既存の`~/.claude/skills/learned/`スキルは引き続き動作
- Stopフックは引き続き実行（ただしv2にもフィードされる）
- 段階的な移行パス: 両方を並行実行可能

## プライバシー

- 観測はマシン上に**ローカル**で保持
- エクスポートできるのは**インスティンクト**（パターン）のみ
- 実際のコードや会話内容は共有されない
- エクスポート内容はユーザーが制御

## 関連リンク

- [Skill Creator](https://skill-creator.app) - リポジトリ履歴からインスティンクトを生成
- [Homunculus](https://github.com/humanplane/homunculus) - v2アーキテクチャのインスピレーション
- [The Longform Guide](https://x.com/affaanmustafa/status/2014040193557471352) - 継続学習セクション

---

*インスティンクトベース学習: 1つの観測ずつ、Claudeにあなたのパターンを教えます。*
