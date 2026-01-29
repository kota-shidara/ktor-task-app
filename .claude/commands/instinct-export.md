---
name: instinct-export
description: インスティンクトをチームメイトや他のプロジェクトと共有するためにエクスポートする
command: /instinct-export
---

# インスティンクトエクスポートコマンド

インスティンクトを共有可能なフォーマットにエクスポートする。以下の用途に最適:
- チームメイトとの共有
- 新しいマシンへの移行
- プロジェクトの規約への貢献

## 使い方

```
/instinct-export                           # 全個人インスティンクトをエクスポート
/instinct-export --domain testing          # testing インスティンクトのみエクスポート
/instinct-export --min-confidence 0.7      # 高信頼度のインスティンクトのみエクスポート
/instinct-export --output team-instincts.yaml
```

## 実行内容

1. `~/.claude/homunculus/instincts/personal/` からインスティンクトを読み込む
2. フラグに基づいてフィルタリング
3. 機密情報を除去:
   - セッションIDを削除
   - ファイルパスを削除 (パターンのみ保持)
   - 「先週」より古いタイムスタンプを削除
4. エクスポートファイルを生成

## 出力フォーマット

YAMLファイルを作成:

```yaml
# Instincts Export
# Generated: 2025-01-22
# Source: personal
# Count: 12 instincts

version: "2.0"
exported_by: "continuous-learning-v2"
export_date: "2025-01-22T10:30:00Z"

instincts:
  - id: prefer-functional-style
    trigger: "when writing new functions"
    action: "Use functional patterns over classes"
    confidence: 0.8
    domain: code-style
    observations: 8

  - id: test-first-workflow
    trigger: "when adding new functionality"
    action: "Write test first, then implementation"
    confidence: 0.9
    domain: testing
    observations: 12

  - id: grep-before-edit
    trigger: "when modifying code"
    action: "Search with Grep, confirm with Read, then Edit"
    confidence: 0.7
    domain: workflow
    observations: 6
```

## プライバシーに関する考慮事項

エクスポートに含まれるもの:
- ✅ トリガーパターン
- ✅ アクション
- ✅ 信頼度スコア
- ✅ ドメイン
- ✅ 観測回数

エクスポートに含まれないもの:
- ❌ 実際のコードスニペット
- ❌ ファイルパス
- ❌ セッショントランスクリプト
- ❌ 個人識別情報

## フラグ

- `--domain <name>`: 指定ドメインのみエクスポート
- `--min-confidence <n>`: 最低信頼度の閾値 (デフォルト: 0.3)
- `--output <file>`: 出力ファイルパス (デフォルト: instincts-export-YYYYMMDD.yaml)
- `--format <yaml|json|md>`: 出力フォーマット (デフォルト: yaml)
- `--include-evidence`: エビデンステキストを含める (デフォルト: 除外)
