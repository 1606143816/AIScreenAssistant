# AI 屏幕助手 文档

AI 屏幕助手 (AIScreenAssistant) Android 项目的完整技术文档。面向开发者、贡献者和希望理解系统架构的读者。

**快速链接**: [架构](./ARCHITECTURE.md) | [接口](./INTERFACES.md) | [开发者指南](./DEVELOPER_GUIDE.md)

---

## 核心文档

### [架构](./ARCHITECTURE.md)
系统设计、技术栈、组件结构和数据流程。从这里开始了解系统如何运作。

### [接口](./INTERFACES.md)
公开的 UseCase 接口、Service 接口、Repository 接口和数据模型。集成或使用此系统的参考。

### [开发者指南](./DEVELOPER_GUIDE.md)
环境搭建、开发工作流、编码规范和常见任务。贡献者必读。

---

## 模块

| 模块 | 描述 | README |
|------|------|--------|
| `core/data` | 数据持久化层，Room 数据库和 Repository 实现 | [README](./模块/core-data.md) |
| `core/domain` | 业务逻辑层，UseCase 实现 | [README](./模块/core-domain.md) |
| `core/model` | 核心数据模型定义 | [README](./模块/core-model.md) |
| `core/serializer` | UI 树序列化工具 | [README](./模块/core-serializer.md) |
| `feature/conversation` | 对话界面模块 | [README](./模块/feature-conversation.md) |
| `feature/settings` | 设置界面模块 | [README](./模块/feature-settings.md) |
| `feature/history` | 历史记录界面模块 | [README](./模块/feature-history.md) |
| `service/accessibility` | 无障碍服务（读取 UI 树 + 执行操作） | [README](./模块/service-accessibility.md) |
| `service/overlay` | 悬浮球覆盖窗口服务 | [README](./模块/service-overlay.md) |
| `di` | Hilt 依赖注入模块 | [README](./模块/di.md) |

---

## 核心概念

| 概念 | 描述 |
|------|------|
| [UI 树 (SerializedUITree)](./专有概念/SerializedUITree.md) | 当前界面结构化描述，包含所有可见元素 |
| [操作 (Action)](./专有概念/Action.md) | 可执行的自动化操作指令 |
| [对话 (Conversation)](./专有概念/Conversation.md) | 用户与 LLM 的交互会话 |
| [分析结果 (AnalysisResult)](./专有概念/AnalysisResult.md) | LLM 返回的界面分析和操作建议 |
| [LLM 配置 (LLMConfig)](./专有概念/LLMConfig.md) | 云端大模型连接配置 |

---

## 入门指南

### 项目新人

按此路径学习：
1. **[架构](./ARCHITECTURE.md)** - 了解全局
2. **[核心概念](#核心概念)** - 学习领域术语
3. **[开发者指南](./DEVELOPER_GUIDE.md)** - 搭建环境
4. **[接口](./INTERFACES.md)** - 探索公开接口

### 首次贡献

1. **[开发者指南](./DEVELOPER_GUIDE.md)** - 搭建和工作流
2. **[开发者指南#常见任务](./DEVELOPER_GUIDE.md)** - 分步指南

---

## 快速参考

### 关键文件

| 文件 | 目的 |
|------|------|
| `app/build.gradle.kts` | 应用构建配置和依赖声明 |
| `app/src/main/AndroidManifest.xml` | 权限声明、组件注册 |
| `app/src/main/java/.../AIHelperApplication.kt` | 应用入口 |
| `.monkeycode/specs/ai-screen-assistant/requirements.md` | 功能需求文档 |
| `.monkeycode/specs/ai-screen-assistant/design.md` | 技术设计文档 |
| `.monkeycode/specs/ai-screen-assistant/tasklist.md` | 实施任务列表 |
