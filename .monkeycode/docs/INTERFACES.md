# 接口文档

## 概述

本文档描述 AI 屏幕助手的核心接口和契约，包括 Domain 层 UseCase 接口、Data 层 Repository 接口、Service 层接口和数据模型定义。

## UseCase 接口

### ReadUITreeUseCase

读取当前前台界面的 UI 树结构。

```
输入: 无
输出: SerializedUITree
实现: 通过 AIAccessibilityService.getWindowRoot() 获取根节点，UITreeSerializer 递归序列化
```

### AnalyzeScreenUseCase

将 UI 树和用户指令发送至 LLM 进行分析。

```
输入: SerializedUITree, String prompt, List<Message> history
输出: Result<AnalysisResult>
实现: FilterSensitiveNodesUseCase -> LLMRepository.analyze()
```

### ExecuteActionUseCase

逐条执行操作序列。

```
输入: List<Action>
输出: Flow<ActionResult>
实现: 通过 AIAccessibilityService.performAction() 逐条执行
       连续 3 次失败自动暂停，返回 Flow 通知每步结果
```

### ManageConversationUseCase

管理对话生命周期。

```
输入: Conversation, Operation (CREATE/APPEND/END)
输出: Conversation
实现: ConversationRepository 的 CRUD 封装
```

### ProcessVoiceInputUseCase

语音转文字。

```
输入: AudioData
输出: Result<String> (transcribed text)
实现: Android SpeechRecognizer API
```

### ValidateLLMConfigUseCase

验证 LLM 服务配置。

```
输入: LLMConfig
输出: ValidationResult (含连通状态和模型可用性)
实现: 通过 LLMRepository 发送测试请求
```

### FilterSensitiveNodesUseCase

过滤 UI 树中的敏感信息。

```
输入: SerializedUITree
输出: SerializedUITree (清洗后)
实现: 遍历 elements，将 isPassword=true 的节点 text 替换为 "[已隐藏]"
```

## Repository 接口

### LLMRepository

```
analyze(uiTree: SerializedUITree, prompt: String, history: List<Message>): Result<AnalysisResult>
validateConnection(config: LLMConfig): Result<ValidationResult>
```

### ConversationRepository

```
getHistory(): Flow<List<ConversationSummary>>
getConversation(id: String): Flow<Conversation?>
getAll(): Flow<List<Conversation>>
insert(conversation: Conversation): Long
delete(id: String)
```

### SettingsRepository

```
getLLMConfig(): Flow<LLMConfig?>
saveLLMConfig(config: LLMConfig)
getOperationMode(): Flow<OperationMode>
saveOperationMode(mode: OperationMode)
```

## Service 接口

### AIAccessibilityService

```
readUITree(): SerializedUITree
    - 从 rootInActiveWindow 递归遍历
    - 提取每个 AccessibilityNodeInfo 的关键属性
    - 序列化为 SerializedUITree JSON

performAction(action: Action): ActionResult
    - 根据 Action 类型分发：
      * Click/LongClick -> findAccessibilityNodeInfo(index) + performAction(ACTION_CLICK)
      * InputText -> performAction(ACTION_SET_TEXT)
      * Swipe -> dispatchGesture
      * PressBack -> performGlobalAction(GLOBAL_ACTION_BACK)
      * ScrollForward/Backward -> performAction(ACTION_SCROLL_FORWARD/BACKWARD)
      * OpenApp -> context.startActivity(launchIntent)
    - 返回 ActionResult.Success 或 ActionResult.Failure
```

### OverlayService

```
交互事件 (通过 EventBus / SharedFlow):
    - onFloatClick: 触发 UI 树读取 → 打开对话界面
    - onFloatLongPress: 启动语音输入
    - onFloatDrag: 更新悬浮球位置
    - onFloatDismiss: 最小化至通知栏
```

## 数据模型

### SerializedUITree

```kotlin
data class SerializedUITree(
    val packageName: String,         // 当前前台应用包名
    val activityName: String?,       // 当前 Activity
    val elements: List<UIElement>,   // 可见元素列表
    val timestamp: Long              // 采集时间
)

data class UIElement(
    val id: String?,                 // resource-id
    val type: String,                // 控件类名
    val text: String?,               // 可见文本
    val contentDescription: String?, // 无障碍描述
    val hint: String?,               // 提示文字
    val bounds: Rect,                // 边界坐标
    val isClickable: Boolean,
    val isEditable: Boolean,
    val isPassword: Boolean,         // 是否为密码字段
    val isChecked: Boolean?,
    val isScrollable: Boolean,
    val isFocused: Boolean,
    val childCount: Int,
    val depth: Int                   // 层级深度
)
```

### AnalysisResult

```kotlin
data class AnalysisResult(
    val screenDescription: String,               // 界面描述
    val keyElements: List<UIElementReference>,   // 关键可操作元素
    val suggestionText: String,                  // 操作建议文本
    val actions: List<Action>                    // 操作指令序列
)

data class UIElementReference(
    val elementIndex: Int,     // 指向 elements 列表的下标
    val label: String,         // 元素标签
    val description: String    // 元素描述
)
```

### Action

```kotlin
sealed class Action {
    data class Click(val elementIndex: Int) : Action()
    data class LongClick(val elementIndex: Int) : Action()
    data class InputText(val elementIndex: Int, val text: String) : Action()
    data class Swipe(
        val startX: Int, val startY: Int,
        val endX: Int, val endY: Int,
        val duration: Long = 300
    ) : Action()
    object PressBack : Action()
    data class ScrollForward(val elementIndex: Int) : Action()
    data class ScrollBackward(val elementIndex: Int) : Action()
    data class OpenApp(val packageName: String) : Action()
}
```

### Conversation & Message

```kotlin
data class Conversation(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val messages: List<Message>,
    val uiTreeRecords: List<UITreeRecord>
)

data class Message(
    val id: String,
    val role: MessageRole,
    val content: String,
    val analysisResult: AnalysisResult? = null,
    val timestamp: Long
)

enum class MessageRole { USER, ASSISTANT }
```

### LLMConfig & OperationMode

```kotlin
data class LLMConfig(
    val baseUrl: String,
    val apiKey: String,
    val modelName: String,
    val maxTokens: Int = 4096,
    val temperature: Float = 0.7f
)

enum class OperationMode {
    SUGGESTION,   // 建议模式
    AUTONOMOUS    // 自主执行模式
}
```

## LLM API 契约

### System Prompt

```
你是一个 Android 手机操作助手。用户会提供当前界面的 UI 树结构 (JSON 格式)，
包含每个元素的类型、文字、边界坐标和交互属性。

你的任务是:
1. 理解用户意图
2. 描述当前界面
3. 如果用户想执行操作，给出精确的操作指令序列

输出格式 (JSON):
{
  "screenDescription": "...",
  "keyElements": [{"elementIndex": 0, "label": "搜索框", "description": "..."}],
  "suggestionText": "...",
  "actions": [
    {"type": "CLICK", "elementIndex": 3},
    {"type": "INPUT_TEXT", "elementIndex": 0, "text": "你好"},
    {"type": "SWIPE", "startX": 540, "startY": 1800, "endX": 540, "endY": 400},
    {"type": "PRESS_BACK"},
    {"type": "SCROLL_FORWARD", "elementIndex": 5},
    {"type": "OPEN_APP", "packageName": "com.example.app"}
  ]
}
```

### API 请求格式

```json
POST {baseUrl}/v1/chat/completions
Content-Type: application/json
Authorization: Bearer {apiKey}

{
  "model": "{modelName}",
  "messages": [
    {
      "role": "system",
      "content": "{System Prompt}"
    },
    {
      "role": "user",
      "content": "当前界面 UI 树:\n{SerializedUITree JSON}\n\n用户指令: {prompt}"
    }
  ],
  "max_tokens": 4096,
  "temperature": 0.7
}
```

### API 响应解析

LLM 返回的 `choices[0].message.content` 应为符合 AnalysisResult 结构的 JSON，应用解析该 JSON 并构建 `AnalysisResult` 对象。
