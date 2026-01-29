---
description: 要件を再確認し、リスクを評価し、段階的な実装計画を作成します。コードに手を付ける前にユーザーの確認を待ちます。
---

# Plan コマンド

このコマンドは **planner** エージェントを呼び出し、コードを書く前に包括的な実装計画を作成します。

## このコマンドの機能

1. **要件の再確認** - 構築すべき内容を明確化
2. **リスクの特定** - 潜在的な問題やブロッカーを洗い出す
3. **段階的計画の作成** - 実装をフェーズに分解
4. **確認を待つ** - 続行する前にユーザーの承認を必ず得る

## 使用場面

`/plan` を使う場面:
- 新機能の開発を開始する時
- 大規模なアーキテクチャ変更を行う時
- 複雑なリファクタリングに取り組む時
- 複数のファイル/コンポーネントに影響がある時
- 要件が不明確または曖昧な時

## 動作の仕組み

planner エージェントは以下を行います:

1. **リクエストを分析** し、要件を明確な言葉で再表現する
2. **フェーズに分解** し、具体的で実行可能なステップを定義する
3. **依存関係を特定** する
4. **リスクを評価** し、潜在的なブロッカーを洗い出す
5. **複雑度を見積もる** (高/中/低)
6. **計画を提示** し、明示的な確認を待つ

## 使用例

```
User: /plan I need to add real-time notifications when markets resolve

Agent (planner):
# Implementation Plan: Real-Time Market Resolution Notifications

## Requirements Restatement
- Send notifications to users when markets they're watching resolve
- Support multiple notification channels (in-app, email, webhook)
- Ensure notifications are delivered reliably
- Include market outcome and user's position result

## Implementation Phases

### Phase 1: Database Schema
- Add notifications table with columns: id, user_id, market_id, type, status, created_at
- Add user_notification_preferences table for channel preferences
- Create indexes on user_id and market_id for performance

### Phase 2: Notification Service
- Create notification service in lib/notifications.ts
- Implement notification queue using BullMQ/Redis
- Add retry logic for failed deliveries
- Create notification templates

### Phase 3: Integration Points
- Hook into market resolution logic (when status changes to "resolved")
- Query all users with positions in market
- Enqueue notifications for each user

### Phase 4: Frontend Components
- Create NotificationBell component in header
- Add NotificationList modal
- Implement real-time updates via Supabase subscriptions
- Add notification preferences page

## Dependencies
- Redis (for queue)
- Email service (SendGrid/Resend)
- Supabase real-time subscriptions

## Risks
- HIGH: Email deliverability (SPF/DKIM required)
- MEDIUM: Performance with 1000+ users per market
- MEDIUM: Notification spam if markets resolve frequently
- LOW: Real-time subscription overhead

## Estimated Complexity: MEDIUM
- Backend: 4-6 hours
- Frontend: 3-4 hours
- Testing: 2-3 hours
- Total: 9-13 hours

**WAITING FOR CONFIRMATION**: Proceed with this plan? (yes/no/modify)
```

## 重要な注意事項

**重要**: planner エージェントは、「yes」「proceed」などの明確な承認を受け取るまで、コードを一切書きません。

変更を希望する場合は以下のように回答してください:
- "modify: [変更内容]"
- "different approach: [代替案]"
- "skip phase 2 and do phase 3 first"

## 他のコマンドとの連携

計画策定後:
- `/tdd` でテスト駆動開発による実装
- `/build-and-fix` でビルドエラー発生時の対応
- `/code-review` で完成した実装のレビュー

## 関連エージェント

このコマンドは以下に配置された `planner` エージェントを呼び出します:
`~/.claude/agents/planner.md`
