# core/model

核心数据模型定义，整个项目共享的数据类型。

## 结构

```
core/model/
├── Action.kt              # Action sealed class 及子类型
├── SerializedUITree.kt    # UIElement + SerializedUITree
├── AnalysisResult.kt      # AnalysisResult + UIElementReference
├── Conversation.kt        # Conversation + Message + UITreeRecord + MessageRole
├── LLMConfig.kt           # LLM 配置数据类
├── OperationMode.kt       # 操作模式枚举
└── ActionResult.kt        # 操作执行结果
```

## 关键文件

| 文件 | 目的 |
|------|------|
| `Action.kt` | 所有操作类型的 sealed class 定义（Click, InputText, Swipe 等） |
| `SerializedUITree.kt` | UI 树结构定义，含 UIElement 及其属性 |
| `AnalysisResult.kt` | LLM 返回的分析结果结构 |
| `Conversation.kt` | 对话和消息的数据结构 |

## 规范

- 所有模型使用 Kotlin `data class`
- 枚举使用 `enum class`
- 多态类型使用 `sealed class`（如 Action）
- 字段使用 `val`（不可变）
