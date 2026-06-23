# feature/history

历史记录界面模块，展示过往对话列表和详情。

## 结构

```
feature/history/
├── HistoryScreen.kt           # Compose 历史列表界面
├── HistoryDetailScreen.kt     # 对话详情界面
└── HistoryViewModel.kt        # 历史记录状态管理
```

## 功能

- 时间倒序列表展示
- 对话摘要 + 时间 + 界面描述
- 点击进入完整详情
- 滑动删除对话记录

## 依赖

**本模块依赖**:
- `core/data/` (ConversationRepository)

**依赖本模块的**:
- MainActivity (Navigation)
