# feature/conversation

对话界面模块，包含 ConversationScreen 和 ConversationViewModel。

## 结构

```
feature/conversation/
├── ConversationScreen.kt         # Compose 对话界面
├── ConversationViewModel.kt      # 对话状态管理
└── components/
    ├── MessageBubble.kt          # 消息气泡组件
    ├── InputBar.kt               # 输入栏组件
    ├── ModeSwitchChip.kt         # 操作模式切换芯片
    ├── ActionCard.kt             # 操作建议卡片
    ├── ActionProgressBar.kt      # 操作执行进度
    └── VoiceInputButton.kt       # 语音输入按钮
```

## 依赖

**本模块依赖**:
- `core/domain/` (AnalyzeScreenUseCase, ExecuteActionUseCase 等)
- `core/model/` (Conversation, Message, OperationMode)
- `service/overlay/` (通过 EventBus 接收悬浮球事件)

**依赖本模块的**:
- MainActivity (Navigation)
