# AnalysisResult

AnalysisResult 是 LLM 对当前界面分析后的结构化返回结果，包含界面描述、可操作元素和操作建议。

## 什么是 AnalysisResult？

当 UI 树和用户指令发送至 LLM 后，LLM 返回一个 AnalysisResult 对象，系统解析后展示给用户并据此执行自动化操作。

## 代码位置

| 方面 | 位置 |
|------|------|
| 模型 | `core/model/AnalysisResult.kt` |
| 解析 | `core/data/LLMApiService.kt` |
| 使用 | `core/domain/AnalyzeScreenUseCase.kt` |

## 结构

```kotlin
data class AnalysisResult(
    val screenDescription: String,               // 当前界面的自然语言描述
    val keyElements: List<UIElementReference>,   // LLM 识别的关键可操作元素
    val suggestionText: String,                  // 给用户的操作建议文本
    val actions: List<Action>                    // 可执行的操作指令序列
)

data class UIElementReference(
    val elementIndex: Int,     // 指向 SerializedUITree.elements 的下标
    val label: String,         // 元素标签（如 "搜索框"）
    val description: String    // 元素描述（如 "位于顶部的搜索输入框"）
)
```

### 关键字段

| 字段 | 类型 | 描述 |
|------|------|------|
| `screenDescription` | `String` | 对话中展示给用户的界面功能描述 |
| `keyElements` | `List<UIElementReference>` | LLM 认为与用户意图相关的关键元素 |
| `suggestionText` | `String` | 给用户的操作建议 |
| `actions` | `List<Action>` | 可执行的操作序列（建议模式下单独确认，自主模式下自动执行） |

## LLM 输出格式契约

LLM 返回的 JSON 必须符合以下格式：

```json
{
  "screenDescription": "当前界面是微信聊天列表页，顶部有搜索框，下方是聊天记录列表...",
  "keyElements": [
    {"elementIndex": 0, "label": "搜索框", "description": "顶部的搜索输入框"},
    {"elementIndex": 5, "label": "第一个聊天项", "description": "列表中的第一个联系人"},
    {"elementIndex": 12, "label": "新建聊天按钮", "description": "右上角的加号按钮"}
  ],
  "suggestionText": "您可以在搜索框中输入联系人名字来查找聊天记录，或点击列表中的联系人来打开对话。",
  "actions": [
    {"type": "CLICK", "elementIndex": 12},
    {"type": "INPUT_TEXT", "elementIndex": 0, "text": "张三"}
  ]
}
```

## 不变量

1. **elementIndex 一致性**: keyElements 中的 elementIndex 和 actions 中的 elementIndex 必须与当前 SerializedUITree.elements 下标一致
2. **actions 可空**: 当用户仅询问信息而无操作意图时，actions 可为空列表
