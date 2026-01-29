# Orchestrate コマンド

複雑なタスクのための逐次エージェントワークフロー。

## 使い方

`/orchestrate [workflow-type] [task-description]`

## ワークフロータイプ

### feature
機能実装の完全ワークフロー:
```
planner -> tdd-guide -> code-reviewer -> security-reviewer
```

### bugfix
バグ調査・修正ワークフロー:
```
explorer -> tdd-guide -> code-reviewer
```

### refactor
安全なリファクタリングワークフロー:
```
architect -> code-reviewer -> tdd-guide
```

### security
セキュリティ重視のレビュー:
```
security-reviewer -> code-reviewer -> architect
```

## 実行パターン

ワークフロー内の各エージェントに対して:

1. **エージェントを呼び出す** - 前のエージェントからのコンテキストを渡す
2. **出力を収集する** - 構造化された引き継ぎドキュメントとして
3. **次のエージェントに渡す** - チェーン内の次へ
4. **結果を集約する** - 最終レポートにまとめる

## 引き継ぎドキュメント形式

エージェント間で引き継ぎドキュメントを作成します:

```markdown
## HANDOFF: [previous-agent] -> [next-agent]

### Context
[実施内容の要約]

### Findings
[主要な発見事項や決定事項]

### Files Modified
[変更したファイルの一覧]

### Open Questions
[次のエージェントへの未解決事項]

### Recommendations
[推奨される次のステップ]
```

## 例: 機能実装ワークフロー

```
/orchestrate feature "Add user authentication"
```

実行内容:

1. **Planner エージェント**
   - 要件を分析
   - 実装計画を作成
   - 依存関係を特定
   - 出力: `HANDOFF: planner -> tdd-guide`

2. **TDD Guide エージェント**
   - planner の引き継ぎを読む
   - テストを先に書く
   - テストを通す実装を行う
   - 出力: `HANDOFF: tdd-guide -> code-reviewer`

3. **Code Reviewer エージェント**
   - 実装をレビュー
   - 問題点を確認
   - 改善を提案
   - 出力: `HANDOFF: code-reviewer -> security-reviewer`

4. **Security Reviewer エージェント**
   - セキュリティ監査
   - 脆弱性チェック
   - 最終承認
   - 出力: 最終レポート

## 最終レポート形式

```
ORCHESTRATION REPORT
====================
Workflow: feature
Task: Add user authentication
Agents: planner -> tdd-guide -> code-reviewer -> security-reviewer

SUMMARY
-------
[1段落の要約]

AGENT OUTPUTS
-------------
Planner: [要約]
TDD Guide: [要約]
Code Reviewer: [要約]
Security Reviewer: [要約]

FILES CHANGED
-------------
[変更された全ファイルの一覧]

TEST RESULTS
------------
[テスト成功/失敗の要約]

SECURITY STATUS
---------------
[セキュリティの検出事項]

RECOMMENDATION
--------------
[SHIP / NEEDS WORK / BLOCKED]
```

## 並列実行

独立したチェックの場合、エージェントを並列実行します:

```markdown
### Parallel Phase
同時実行:
- code-reviewer (品質)
- security-reviewer (セキュリティ)
- architect (設計)

### Merge Results
出力を1つのレポートにまとめる
```

## 引数

$ARGUMENTS:
- `feature <description>` - 機能実装の完全ワークフロー
- `bugfix <description>` - バグ修正ワークフロー
- `refactor <description>` - リファクタリングワークフロー
- `security <description>` - セキュリティレビューワークフロー
- `custom <agents> <description>` - カスタムエージェントシーケンス

## カスタムワークフロー例

```
/orchestrate custom "architect,tdd-guide,code-reviewer" "Redesign caching layer"
```

## ヒント

1. **複雑な機能は planner から始める**
2. **マージ前に必ず code-reviewer を含める**
3. **認証・決済・個人情報には security-reviewer を使う**
4. **引き継ぎは簡潔に** - 次のエージェントが必要とする情報に絞る
5. **必要に応じてエージェント間で検証を実行する**
