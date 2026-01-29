---
name: instinct-import
description: チームメイト、Skill Creator、その他のソースからインスティンクトをインポート
command: /instinct-import
implementation: python3 ~/.claude/skills/continuous-learning-v2/scripts/instinct-cli.py import <file>
---

# インスティンクトインポートコマンド

## 実装

```bash
python3 ~/.claude/skills/continuous-learning-v2/scripts/instinct-cli.py import <file-or-url> [--dry-run] [--force] [--min-confidence 0.7]
```

以下からインスティンクトをインポート:
- チームメイトのエクスポート
- Skill Creator（リポジトリ分析）
- コミュニティコレクション
- 以前のマシンのバックアップ

## 使い方

```
/instinct-import team-instincts.yaml
/instinct-import https://github.com/org/repo/instincts.yaml
/instinct-import --from-skill-creator acme/webapp
```

## 処理内容

1. インスティンクトファイルを取得（ローカルパスまたはURL）
2. フォーマットを解析・検証
3. 既存インスティンクトとの重複を確認
4. 新規インスティンクトをマージまたは追加
5. `~/.claude/homunculus/instincts/inherited/`に保存

## インポートプロセス

```
📥 インスティンクトをインポート中: team-instincts.yaml
================================================

12個のインスティンクトが見つかりました。

競合を分析中...

## 新規インスティンクト (8)
以下が追加されます:
  ✓ use-zod-validation (信頼度: 0.7)
  ✓ prefer-named-exports (信頼度: 0.65)
  ✓ test-async-functions (信頼度: 0.8)
  ...

## 重複インスティンクト (3)
類似するインスティンクトが既に存在:
  ⚠️ prefer-functional-style
     ローカル: 信頼度0.8、12回の観測
     インポート: 信頼度0.7
     → ローカルを保持（より高い信頼度）

  ⚠️ test-first-workflow
     ローカル: 信頼度0.75
     インポート: 信頼度0.9
     → インポートで更新（より高い信頼度）

## 競合インスティンクト (1)
ローカルインスティンクトと矛盾:
  ❌ use-classes-for-services
     競合先: avoid-classes
     → スキップ（手動解決が必要）

---
8個の新規をインポート、1個を更新、3個をスキップしますか？
```

## マージ戦略

### 重複の場合
既存のインスティンクトと一致するものをインポートする場合:
- **より高い信頼度を優先**: 信頼度が高い方を保持
- **エビデンスをマージ**: 観測回数を合算
- **タイムスタンプを更新**: 最近検証されたものとしてマーク

### 競合の場合
既存のインスティンクトと矛盾するものをインポートする場合:
- **デフォルトでスキップ**: 競合するインスティンクトはインポートしない
- **レビュー用にフラグ**: 両方に注意が必要とマーク
- **手動解決**: ユーザーがどちらを保持するか決定

## ソース追跡

インポートされたインスティンクトには以下のマークが付きます:
```yaml
source: "inherited"
imported_from: "team-instincts.yaml"
imported_at: "2025-01-22T10:30:00Z"
original_source: "session-observation"  # または "repo-analysis"
```

## Skill Creatorとの連携

Skill Creatorからインポートする場合:

```
/instinct-import --from-skill-creator acme/webapp
```

リポジトリ分析から生成されたインスティンクトを取得:
- ソース: `repo-analysis`
- より高い初期信頼度（0.7以上）
- ソースリポジトリにリンク

## フラグ

- `--dry-run`: インポートせずにプレビュー
- `--force`: 競合があってもインポート
- `--merge-strategy <higher|local|import>`: 重複の処理方法
- `--from-skill-creator <owner/repo>`: Skill Creator分析からインポート
- `--min-confidence <n>`: しきい値以上のインスティンクトのみインポート

## 出力

インポート後:
```
✅ インポート完了！

追加: 8個のインスティンクト
更新: 1個のインスティンクト
スキップ: 3個のインスティンクト（重複2個、競合1個）

新規インスティンクトの保存先: ~/.claude/homunculus/instincts/inherited/

/instinct-status ですべてのインスティンクトを確認できます。
```
