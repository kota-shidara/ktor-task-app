---
name: instinct-status
description: すべての学習済みインスティンクトと信頼度レベルを表示
command: /instinct-status
implementation: python3 ~/.claude/skills/continuous-learning-v2/scripts/instinct-cli.py status
---

# インスティンクトステータスコマンド

すべての学習済みインスティンクトと信頼度スコアをドメイン別にグループ化して表示します。

## 実装

```bash
python3 ~/.claude/skills/continuous-learning-v2/scripts/instinct-cli.py status
```

## 使い方

```
/instinct-status
/instinct-status --domain code-style
/instinct-status --low-confidence
```

## 処理内容

1. `~/.claude/homunculus/instincts/personal/`からすべてのインスティンクトファイルを読み取り
2. `~/.claude/homunculus/instincts/inherited/`から継承インスティンクトを読み取り
3. ドメイン別にグループ化し、信頼度バーとともに表示

## 出力フォーマット

```
📊 インスティンクトステータス
==================

## コードスタイル (4 インスティンクト)

### prefer-functional-style
トリガー: when writing new functions
アクション: Use functional patterns over classes
信頼度: ████████░░ 80%
ソース: session-observation | 最終更新: 2025-01-22

### use-path-aliases
トリガー: when importing modules
アクション: Use @/ path aliases instead of relative imports
信頼度: ██████░░░░ 60%
ソース: repo-analysis (github.com/acme/webapp)

## テスティング (2 インスティンクト)

### test-first-workflow
トリガー: when adding new functionality
アクション: Write test first, then implementation
信頼度: █████████░ 90%
ソース: session-observation

## ワークフロー (3 インスティンクト)

### grep-before-edit
トリガー: when modifying code
アクション: Search with Grep, confirm with Read, then Edit
信頼度: ███████░░░ 70%
ソース: session-observation

---
合計: 9 インスティンクト（個人4、継承5）
Observer: 実行中（最終分析: 5分前）
```

## フラグ

- `--domain <name>`: ドメインでフィルタ（code-style、testing、gitなど）
- `--low-confidence`: 信頼度0.5未満のインスティンクトのみ表示
- `--high-confidence`: 信頼度0.7以上のインスティンクトのみ表示
- `--source <type>`: ソースでフィルタ（session-observation、repo-analysis、inherited）
- `--json`: プログラム利用のためJSON形式で出力
