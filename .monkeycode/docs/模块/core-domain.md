# core/domain

业务逻辑层，实现所有 UseCase，作为 UI 层和 Data 层之间的协调者。

## 结构

```
core/domain/
├── ReadUITreeUseCase.kt           # 读取 UI 树
├── AnalyzeScreenUseCase.kt        # 分析屏幕（核心协调）
├── ExecuteActionUseCase.kt        # 执行操作序列
├── ManageConversationUseCase.kt   # 管理对话
├── ProcessVoiceInputUseCase.kt    # 语音转文字
├── ValidateLLMConfigUseCase.kt    # 验证 LLM 配置
└── FilterSensitiveNodesUseCase.kt # 过滤敏感节点
```

## 关键文件

| 文件 | 目的 | 依赖 |
|------|------|------|
| `ReadUITreeUseCase.kt` | 通过与 AccessibilityService 通信读取 UI 树 | AIAccessibilityService |
| `AnalyzeScreenUseCase.kt` | 协调 ReadUITree → FilterSensitive → LLMRepository | ReadUITreeUseCase + FilterSensitiveNodesUseCase + LLMRepository |
| `ExecuteActionUseCase.kt` | 调度 Action 序列执行，管理失败计数 | AIAccessibilityService |
| `ManageConversationUseCase.kt` | 创建/追加/结束对话 | ConversationRepository |
| `ProcessVoiceInputUseCase.kt` | 封装 SpeechRecognizer API | Android SpeechRecognizer |
| `ValidateLLMConfigUseCase.kt` | 发送测试请求验证 LLM 配置 | LLMRepository |
| `FilterSensitiveNodesUseCase.kt` | 检测 isPassword 节点并替换文本 | - |

## 规范

### 命名约定
- 每个 UseCase 一个文件，文件名 = UseCase 类名
- 使用 `operator fun invoke()` 作为主入口

### 代码模式

```kotlin
class ExampleUseCase @Inject constructor(
    private val repository: SomeRepository,
    private val otherUseCase: OtherUseCase
) {
    suspend operator fun invoke(input: Input): Result<Output> {
        // 协调多个依赖完成业务逻辑
    }
}
```

### 错误处理
- 使用 Kotlin `Result<T>` 类型包装返回值
- 不直接抛出异常，通过 Result.failure 传递错误
- ExecuteActionUseCase 使用 sealed class 表示操作结果

## 依赖

**本模块依赖**:
- `core/data/` - Repository 接口
- `core/model/` - 数据模型
- `service/accessibility/` - AccessibilityService（通过接口）

**依赖本模块的**:
- `feature/*/` - ViewModel 通过 UseCase 获取数据
