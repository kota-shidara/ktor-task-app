# Checkpoint コマンド

ワークフロー内でチェックポイントを作成または検証する。

## 使い方

`/checkpoint [create|verify|list] [name]`

## チェックポイントの作成

チェックポイント作成時:

1. `/verify quick` を実行して現在の状態がクリーンであることを確認
2. チェックポイント名を付けて git stash またはコミットを作成
3. チェックポイントを `.claude/checkpoints.log` に記録:

```bash
echo "$(date +%Y-%m-%d-%H:%M) | $CHECKPOINT_NAME | $(git rev-parse --short HEAD)" >> .claude/checkpoints.log
```

4. チェックポイント作成完了を報告

## チェックポイントの検証

チェックポイントとの比較検証時:

1. ログからチェックポイントを読み込む
2. 現在の状態とチェックポイントを比較:
   - チェックポイント以降に追加されたファイル
   - チェックポイント以降に変更されたファイル
   - テスト合格率の変化
   - カバレッジの変化

3. レポート出力:
```
CHECKPOINT COMPARISON: $NAME
============================
Files changed: X
Tests: +Y passed / -Z failed
Coverage: +X% / -Y%
Build: [PASS/FAIL]
```

## チェックポイント一覧

全チェックポイントを以下の情報とともに表示:
- 名前
- タイムスタンプ
- Git SHA
- ステータス (current, behind, ahead)

## ワークフロー

一般的なチェックポイントの流れ:

```
[開始] --> /checkpoint create "feature-start"
   |
[実装] --> /checkpoint create "core-done"
   |
[テスト] --> /checkpoint verify "core-done"
   |
[リファクタリング] --> /checkpoint create "refactor-done"
   |
[PR] --> /checkpoint verify "feature-start"
```

## 引数

$ARGUMENTS:
- `create <name>` - 名前付きチェックポイントを作成
- `verify <name>` - 名前付きチェックポイントと比較検証
- `list` - 全チェックポイントを表示
- `clear` - 古いチェックポイントを削除 (直近5件は保持)
