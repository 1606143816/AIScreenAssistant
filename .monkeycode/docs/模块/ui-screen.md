# UI Screen 模块

应用的所有 Compose 界面组件。

## Screen 组件

| Screen | 文件 | 职责 |
|--------|------|------|
| `HomeScreen` | `HomeScreen.kt` | 应用首页：状态卡片（无障碍服务、LLM 配置）、网络离线横幅、功能入口按钮 |
| `ConversationScreen` | `ConversationScreen.kt` | 对话界面：消息列表（用户/AI 气泡）、输入栏、语音按钮、模式切换芯片、分析结果卡片 |
| `SettingsScreen` | `SettingsScreen.kt` | 设置界面：LLM 配置表单（API 地址/密钥/模型）、连接验证、操作模式选择 |
| `HistoryScreen` | `HistoryScreen.kt` | 历史界面：对话列表（摘要+时间）、对话详情查看、删除操作 |

## 子组件

### ModeSwitchChip
定义于 `ConversationScreen.kt`，在对话界面顶栏显示操作模式切换芯片：

| 模式 | 颜色 | 说明 |
|------|------|------|
| SUGGESTION | `secondaryContainer` | LLM 分析后提供建议，用户决定是否执行 |
| AUTONOMOUS | `tertiaryContainer` | LLM 自动分析并执行操作 |

### MessageBubble
用户消息和 AI 回复的气泡组件：
- 用户：右对齐，`primaryContainer` 背景
- AI：左对齐，`surfaceVariant` 背景，含 `AnalysisResultCard`

### AnalysisResultCard
显示 LLM 返回的分析结果：
- 界面描述 (`screenDescription`)
- 操作建议 (`suggestionText`)
- 操作计划步骤数
- 关键元素标签

### InputBar
底部输入区域：语音按钮 (Mic) + 文本输入 + 发送按钮。

### StatusCard
首页状态指示卡片，根据 enabled 状态切换绿色/红色容器色。

## 依赖

所有 Screen 通过函数参数接收状态回调，不直接依赖 ViewModel。ViewModels 通过 `hiltViewModel()` 在 `NavGraph` 层级获取后注入。
