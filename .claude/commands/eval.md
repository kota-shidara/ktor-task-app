# Eval コマンド

eval駆動開発ワークフローを管理する。

## 使い方

`/eval [define|check|report|list] [feature-name]`

## Evalの定義

`/eval define feature-name`

新しいeval定義を作成する:

1. 以下のテンプレートで `.claude/evals/feature-name.md` を作成:

```markdown
## EVAL: feature-name
Created: $(date)

### 機能Eval
- [ ] [機能1の説明]
- [ ] [機能2の説明]

### リグレッションEval
- [ ] [既存の動作1が引き続き動作する]
- [ ] [既存の動作2が引き続き動作する]

### 成功基準
- 機能evalのpass@3 > 90%
- リグレッションevalのpass^3 = 100%
```

2. ユーザーに具体的な基準の入力を促す

## Evalの確認

`/eval check feature-name`

機能のevalを実行する:

1. `.claude/evals/feature-name.md` からeval定義を読み込む
2. 各機能evalに対して:
   - 基準の検証を試行
   - PASS/FAILを記録
   - `.claude/evals/feature-name.log` に試行を記録
3. 各リグレッションevalに対して:
   - 関連テストを実行
   - ベースラインと比較
   - PASS/FAILを記録
4. 現在のステータスを報告:

```
EVAL CHECK: feature-name
========================
Capability: X/Y passing
Regression: X/Y passing
Status: IN PROGRESS / READY
```

## Evalレポート

`/eval report feature-name`

包括的なevalレポートを生成:

```
EVAL REPORT: feature-name
=========================
Generated: $(date)

CAPABILITY EVALS
----------------
[eval-1]: PASS (pass@1)
[eval-2]: PASS (pass@2) - required retry
[eval-3]: FAIL - see notes

REGRESSION EVALS
----------------
[test-1]: PASS
[test-2]: PASS
[test-3]: PASS

METRICS
-------
Capability pass@1: 67%
Capability pass@3: 100%
Regression pass^3: 100%

NOTES
-----
[問題点、エッジケース、所見]

RECOMMENDATION
--------------
[SHIP / NEEDS WORK / BLOCKED]
```

## Eval一覧

`/eval list`

全eval定義を表示:

```
EVAL DEFINITIONS
================
feature-auth      [3/5 passing] IN PROGRESS
feature-search    [5/5 passing] READY
feature-export    [0/4 passing] NOT STARTED
```

## 引数

$ARGUMENTS:
- `define <name>` - 新しいeval定義を作成
- `check <name>` - evalを実行・確認
- `report <name>` - 完全なレポートを生成
- `list` - 全evalを表示
- `clean` - 古いevalログを削除 (直近10回分は保持)
