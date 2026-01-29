# フックシステム

## フックの種類

- **PreToolUse**: ツール実行前（バリデーション、パラメータ変更）
- **PostToolUse**: ツール実行後（自動フォーマット、チェック）
- **Stop**: セッション終了時（最終検証）

## 現在のフック（~/.claude/settings.json 内）

### PreToolUse
- **tmux リマインダー**: 長時間実行コマンド（npm, pnpm, yarn, cargo 等）に tmux の使用を提案
- **git push レビュー**: プッシュ前に Zed でレビューを開く
- **ドキュメントブロッカー**: 不要な .md/.txt ファイルの作成をブロック

### PostToolUse
- **PR 作成**: PR の URL と GitHub Actions のステータスをログ出力
- **Prettier**: 編集後に JS/TS ファイル（frontend/BFF）を自動フォーマット
- **TypeScript チェック**: frontend/BFF の .ts/.tsx ファイル編集後に tsc を実行（backend の .kt ファイルは対象外）
- **console.log 警告**: 編集ファイル内の console.log について警告

### Stop
- **console.log 監査**: セッション終了前に全変更ファイルの console.log をチェック

## 自動承認パーミッション

慎重に使用すること:
- 信頼できる明確な計画に対して有効化する
- 探索的な作業では無効化する
- dangerously-skip-permissions フラグは絶対に使用しない
- 代わりに `~/.claude.json` の `allowedTools` を設定する

## TodoWrite のベストプラクティス

TodoWrite ツールの活用方法:
- マルチステップタスクの進捗管理
- 指示内容の理解確認
- リアルタイムでの方向修正
- 詳細な実装ステップの表示

Todo リストで明らかになること:
- 順序の誤り
- 欠落している項目
- 不要な余分な項目
- 粒度の不適切さ
- 要件の誤解
