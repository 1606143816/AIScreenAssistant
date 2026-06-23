# 开发者指南

## 项目目的

AI 屏幕助手 (AIScreenAssistant) 是一款 Android 应用，通过 AccessibilityService 读取界面 UI 树并发送至云端 LLM 进行分析，实现手机操作辅助。

**核心职责**:
- 读取任意 Android 应用的前台界面结构化信息
- 将界面描述发送至云端 LLM 获取分析和操作建议
- 通过无障碍服务执行自动化操作（点击、输入、滑动等）

## 环境搭建

### 前置条件

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17 或更高版本
- Gradle 8.7（使用 wrapper 自动下载）
- Android SDK 34（通过 Android Studio SDK Manager 安装）

### 安装

```bash
# 克隆仓库
git clone <repo-url>
cd <repo-name>

# 使用 Android Studio 打开项目
# File → Open → 选择项目根目录

# 或在命令行安装依赖
./gradlew build
```

### 运行

```bash
# 构建 Debug APK
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug

# 运行测试
./gradlew test

# 运行 Lint
./gradlew lint
```

### 设备要求

- Android 8.0 (API 26) 或更高版本
- 需要在系统设置中手动开启无障碍服务（系统设置 → 无障碍 → AI 屏幕助手）

## 开发工作流

### 构建命令

| 命令 | 目的 |
|------|------|
| `./gradlew assembleDebug` | 构建 Debug APK |
| `./gradlew test` | 运行单元测试 |
| `./gradlew connectedAndroidTest` | 运行仪器测试（需连接设备） |
| `./gradlew lint` | 代码检查 |

### 模块依赖关系

```
feature/* (UI)
  └── core/domain (UseCase)
        ├── core/data (Repository)
        │     ├── Room Database
        │     └── OkHttp (LLM API)
        ├── core/model (数据模型)
        └── core/serializer (UI 树序列化)
  └── service/* (Android Services)
```

### 分支策略

- `master` - 主分支
- `feature/*` - 新功能分支
- `fix/*` - Bug 修复分支

## 常见任务

### 添加新的 UseCase

**需创建/修改的文件**:
1. `core/domain/[NewUseCase].kt` - UseCase 实现
2. `di/UseCaseModule.kt` - 注册到 Hilt

**示例**:
```kotlin
class NewUseCase @Inject constructor(
    private val repo: SomeRepository
) {
    suspend operator fun invoke(input: Input): Result<Output> {
        // 实现逻辑
    }
}
```

### 添加新的 Compose 界面

**需创建/修改的文件**:
1. `feature/[newfeature]/[NewFeature]Screen.kt` - UI 界面
2. `feature/[newfeature]/[NewFeature]ViewModel.kt` - ViewModel
3. 在 MainActivity 的 NavHost 中添加路由

### 修改 LLM Prompt

编辑 `LLMApiService` 中的 system prompt 常量。会影响的文件:
- `core/data/LLMApiService.kt` - Prompt 定义和请求构造
- `.monkeycode/docs/INTERFACES.md` - 同步接口文档

### 添加新的 Action 类型

**需修改的文件**:
1. `core/model/Action.kt` - 添加新的 sealed class 子类
2. `service/accessibility/AIAccessibilityService.kt` - 实现执行逻辑
3. `LLMApiService.kt` - 更新 System Prompt 告知 LLM 新 Action 类型

## 编码规范

### 文件组织
- 每个文件一个类
- 文件以其主要类命名
- 相关文件放在同一包

### 命名

| 类型 | 约定 | 示例 |
|------|------|------|
| 文件 | PascalCase | `AIAccessibilityService.kt` |
| 类 | PascalCase | `AIAccessibilityService` |
| 函数 | camelCase | `readUITree` |
| 常量 | SCREAMING_SNAKE | `MAX_RETRY_COUNT` |

### 错误处理

```kotlin
// 推荐：使用 Kotlin Result 类型
fun analyze(): Result<AnalysisResult> {
    return try {
        Result.success(doAnalyze())
    } catch (e: IOException) {
        Result.failure(e)
    }
}

// 使用 sealed class 表示状态
sealed class ActionResult {
    data class Success(val message: String) : ActionResult()
    data class Failure(val error: Throwable) : ActionResult()
}
```

### 测试

- 单元测试文件: `[name]Test.kt` 与源码同目录 (test source set)
- 使用 MockK 进行 mock
- 测试命名: "should [预期行为] when [条件]"
