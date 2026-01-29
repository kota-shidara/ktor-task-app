---
name: continuous-learning
description: Claude Codeセッションから再利用可能なパターンを自動的に抽出し、学習済みスキルとして保存する。
---

# 継続学習スキル

Claude Codeセッション終了時に自動的に評価を行い、学習済みスキルとして保存できる再利用可能なパターンを抽出します。

## 仕組み

このスキルはセッション終了時に**Stopフック**として実行されます:

1. **セッション評価**: セッションに十分なメッセージがあるか確認（デフォルト: 10以上）
2. **パターン検出**: セッションから抽出可能なパターンを特定
3. **スキル抽出**: 有用なパターンを`~/.claude/skills/learned/`に保存

## 設定

`config.json`を編集してカスタマイズ:

```json
{
  "min_session_length": 10,
  "extraction_threshold": "medium",
  "auto_approve": false,
  "learned_skills_path": "~/.claude/skills/learned/",
  "patterns_to_detect": [
    "error_resolution",
    "user_corrections",
    "workarounds",
    "debugging_techniques",
    "project_specific"
  ],
  "ignore_patterns": [
    "simple_typos",
    "one_time_fixes",
    "external_api_issues"
  ]
}
```

## パターンタイプ

| パターン | 説明 |
|---------|-------------|
| `error_resolution` | 特定のエラーがどのように解決されたか |
| `user_corrections` | ユーザーの修正から得られたパターン |
| `workarounds` | フレームワーク/ライブラリの癖への解決策 |
| `debugging_techniques` | 効果的なデバッグアプローチ |
| `project_specific` | プロジェクト固有の規約 |

## フックのセットアップ

`~/.claude/settings.json`に追加:

```json
{
  "hooks": {
    "Stop": [{
      "matcher": "*",
      "hooks": [{
        "type": "command",
        "command": "~/.claude/skills/continuous-learning/evaluate-session.sh"
      }]
    }]
  }
}
```

## なぜStopフックなのか？

- **軽量**: セッション終了時に1回だけ実行
- **非ブロッキング**: すべてのメッセージにレイテンシを追加しない
- **完全なコンテキスト**: セッション全体のトランスクリプトにアクセス可能

## 関連リンク

- [The Longform Guide](https://x.com/affaanmustafa/status/2014040193557471352) - 継続学習セクション
- `/learn` コマンド - セッション中の手動パターン抽出

---

## 比較メモ（調査: 2025年1月）

### vs Homunculus (github.com/humanplane/homunculus)

Homunculus v2はより洗練されたアプローチを採用:

| 機能 | 本アプローチ | Homunculus v2 |
|---------|--------------|---------------|
| 観測 | Stopフック（セッション終了時） | PreToolUse/PostToolUseフック（100%信頼性） |
| 分析 | メインコンテキスト | バックグラウンドエージェント（Haiku） |
| 粒度 | 完全なスキル | アトミック「インスティンクト」 |
| 信頼度 | なし | 0.3-0.9の重み付き |
| 進化 | 直接スキル化 | インスティンクト → クラスタ → スキル/コマンド/エージェント |
| 共有 | なし | インスティンクトのエクスポート/インポート |

**Homunculusからの重要な知見:**
> 「v1はスキルに依存して観測していました。スキルは確率的で、50-80%の確率で発火します。v2は観測にフック（100%信頼性）を使用し、インスティンクトを学習済み行動のアトミック単位とします。」

### v2への潜在的な強化

1. **インスティンクトベース学習** - 信頼度スコアリング付きの小さなアトミック行動
2. **バックグラウンドObserver** - 並列分析するHaikuエージェント
3. **信頼度減衰** - 矛盾した場合にインスティンクトの信頼度が低下
4. **ドメインタグ付け** - code-style、testing、git、debuggingなど
5. **進化パス** - 関連インスティンクトをスキル/コマンドにクラスタリング

参照: `/Users/affoon/Documents/tasks/12-continuous-learning-v2.md`（完全な仕様）
