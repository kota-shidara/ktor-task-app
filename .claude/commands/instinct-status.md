---
name: instinct-status
description: 学習済みの全インスティンクトとその信頼度を表示する
command: /instinct-status
implementation: python3 ~/.claude/skills/continuous-learning-v2/scripts/instinct-cli.py status
---

# インスティンクトステータスコマンド

学習済みの全インスティンクトを信頼度スコアとともにドメイン別にグループ化して表示する。

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

## 実行内容

1. `~/.claude/homunculus/instincts/personal/` から全インスティンクトファイルを読み込む
2. `~/.claude/homunculus/instincts/inherited/` から継承インスティンクトを読み込む
3. 信頼度バーとともにドメイン別にグループ化して表示

## 出力フォーマット

```
📊 Instinct Status
==================

## Code Style (4 instincts)

### prefer-functional-style
Trigger: when writing new functions
Action: Use functional patterns over classes
Confidence: ████████░░ 80%
Source: session-observation | Last updated: 2025-01-22

### use-path-aliases
Trigger: when importing modules
Action: Use @/ path aliases instead of relative imports
Confidence: ██████░░░░ 60%
Source: repo-analysis (github.com/acme/webapp)

## Testing (2 instincts)

### test-first-workflow
Trigger: when adding new functionality
Action: Write test first, then implementation
Confidence: █████████░ 90%
Source: session-observation

## Workflow (3 instincts)

### grep-before-edit
Trigger: when modifying code
Action: Search with Grep, confirm with Read, then Edit
Confidence: ███████░░░ 70%
Source: session-observation

---
Total: 9 instincts (4 personal, 5 inherited)
Observer: Running (last analysis: 5 min ago)
```

## フラグ

- `--domain <name>`: ドメインでフィルタリング (code-style, testing, git など)
- `--low-confidence`: 信頼度 < 0.5 のインスティンクトのみ表示
- `--high-confidence`: 信頼度 >= 0.7 のインスティンクトのみ表示
- `--source <type>`: ソースでフィルタリング (session-observation, repo-analysis, inherited)
- `--json`: プログラムでの利用向けにJSON形式で出力
