# 系统架构

## 概述

AI 屏幕助手是一款 Android 客户端应用，通过 AccessibilityService 读取当前界面的 UI 树结构，将界面的结构化文本描述发送至云端大语言模型 (LLM) 进行分析，同时通过 AccessibilityService 执行自动化操作。应用支持建议模式和自主执行模式双模式切换，提供悬浮球、语音输入等快捷交互入口。

## 技术栈

**语言与运行时**
- Kotlin 1.9.24
- JVM 17
- Android SDK 34 (min 26)

**框架**
- Jetpack Compose (BOM 2024.06.00) - UI 框架
- Jetpack Navigation Compose 2.7.7 - 页面导航
- Hilt 2.51.1 - 依赖注入
- Room 2.6.1 - 本地数据库
- DataStore 1.1.1 - 键值存储

**网络**
- OkHttp 4.12.0 - HTTP 客户端
- kotlinx-serialization-json 1.7.1 - JSON 序列化

**异步**
- Kotlin Coroutines 1.8.1
- Kotlin Flow - 响应式数据流

**测试**
- JUnit 4.13.2 - 单元测试
- MockK 1.13.12 - Mock 框架
- Compose UI Test - UI 测试

**Android 系统 API**
- AccessibilityService - UI 树读取和操作执行
- WindowManager - 悬浮窗管理
- SpeechRecognizer - 语音识别

## 项目结构

```
workspace/
├── app/                                    # 应用模块
│   ├── build.gradle.kts                    # 模块构建配置
│   └── src/main/
│       ├── AndroidManifest.xml             # 清单文件
│       ├── res/                            # 资源文件
│       │   ├── values/                     # 字符串、颜色、主题
│       │   └── xml/                        # 无障碍服务配置
│       └── java/com/monkeycode/aiscreen/
│           ├── AIHelperApplication.kt      # 应用入口
│           ├── MainActivity.kt             # (待实现) 主 Activity
│           ├── core/                       # 核心层
│           │   ├── data/                   # Repository 实现
│           │   ├── domain/                 # UseCase 实现
│           │   ├── model/                  # 数据模型
│           │   └── serializer/             # UI 树序列化
│           ├── feature/                    # 功能界面
│           │   ├── conversation/           # 对话界面
│           │   ├── settings/               # 设置界面
│           │   └── history/                # 历史记录界面
│           ├── service/                    # Android 服务
│           │   ├── accessibility/          # 无障碍服务
│           │   └── overlay/                # 悬浮球服务
│           └── di/                         # 依赖注入模块
├── build.gradle.kts                        # 项目级构建配置
└── settings.gradle.kts                     # 项目设置
```

### 包结构说明

| 包 | 层级 | 职责 |
|----|------|------|
| `core.model` | 核心 | 数据模型定义（Action, Conversation, Message 等） |
| `core.domain` | 核心 | 业务逻辑 UseCase |
| `core.data` | 核心 | Repository 实现，Room DAO |
| `core.serializer` | 核心 | AccessibilityNodeInfo -> SerializedUITree 转换 |
| `feature.*` | 功能 | Compose UI 页面 + ViewModel |
| `service.accessibility` | 服务 | Android AccessibilityService |
| `service.overlay` | 服务 | Android Overlay Service |
| `di` | 基础设施 | Hilt Module |

## 系统架构

```mermaid
flowchart TD
    subgraph "UI Layer (Compose)"
        MA["MainActivity"]
        CS["ConversationScreen"]
        SS["SettingsScreen"]
        HS["HistoryScreen"]
        HoS["HomeScreen"]
    end

    subgraph "Domain Layer"
        RU["ReadUITreeUseCase"]
        AS["AnalyzeScreenUseCase"]
        EA["ExecuteActionUseCase"]
        MC["ManageConversationUseCase"]
        PV["ProcessVoiceInputUseCase"]
        FS["FilterSensitiveNodesUseCase"]
        VL["ValidateLLMConfigUseCase"]
    end

    subgraph "Data Layer"
        LR["LLMRepository"]
        CR["ConversationRepository"]
        SR["SettingsRepository"]
        OK["LLMApiService (OkHttp)"]
        DB[("Room Database")]
        DS[("DataStore")]
    end

    subgraph "Android Services"
        AIAS["AIAccessibilityService"]
        OS["OverlayService"]
    end

    subgraph "External"
        LLM["云端 LLM API"]
    end

    MA --> CS
    MA --> SS
    MA --> HS
    MA --> HoS
    CS --> AS
    CS --> EA
    CS --> MC
    CS --> PV
    AS --> RU
    AS --> FS
    AS --> LR
    EA --> AIAS
    RU --> AIAS
    PV --> SR["SpeechRecognizer"]
    LR --> OK
    OK --> LLM
    MC --> CR
    CR --> DB
    SR["SettingsRepository"] --> DS
    OS --> AIAS
```

## 核心交互流

### 分析流程

```mermaid
sequenceDiagram
    participant User
    participant Overlay as OverlayService
    participant Conv as ConversationScreen
    participant Read as ReadUITreeUseCase
    participant AServ as AIAccessibilityService
    participant Filter as FilterSensitiveNodesUseCase
    participant Analyze as AnalyzeScreenUseCase
    participant LLM as LLMRepository
    participant API as 云端 LLM

    User->>Overlay: 点击悬浮球
    Overlay->>Conv: 触发分析
    Conv->>Read: readUITree()
    Read->>AServ: getWindowRoot()
    AServ-->>Read: AccessibilityNodeInfo 树
    Read-->>Conv: SerializedUITree (JSON)
    Conv->>Analyze: analyze(uiTree, prompt, history)
    Analyze->>Filter: filter(uiTree)
    Filter-->>Analyze: 清洗后的 UI 树
    Analyze->>LLM: analyze(uiTree, prompt, history)
    LLM->>API: POST /chat/completions
    API-->>LLM: AnalysisResult JSON
    LLM-->>Analyze: AnalysisResult
    Analyze-->>Conv: 分析结果
    Conv-->>User: 展示界面描述和操作建议
```

### 操作执行流程

```mermaid
sequenceDiagram
    participant Conv as ConversationScreen
    participant Exec as ExecuteActionUseCase
    participant AServ as AIAccessibilityService
    participant User

    Conv->>Exec: execute(actions)
    loop 逐条执行
        Exec->>AServ: performAction(action)
        alt 成功
            AServ-->>Exec: ActionResult.Success
        else 失败
            AServ-->>Exec: ActionResult.Failure
        end
        Exec-->>Conv: ActionResult
        Conv-->>User: 更新操作进度
    end
```

## 数据模型关系

```mermaid
erDiagram
    Conversation ||--o{ Message : contains
    Conversation ||--o{ UITreeRecord : snapshots
    Message ||--o| AnalysisResult : has
    AnalysisResult ||--o{ UIElementReference : references
    AnalysisResult ||--o{ Action : actions
    SerializedUITree ||--o{ UIElement : elements
    LLMConfig ||--|| SettingsRepository : stored_in
    OperationMode ||--|| SettingsRepository : stored_in
