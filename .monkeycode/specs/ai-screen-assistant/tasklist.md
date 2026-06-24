# 需求实施计划

- [x] 1. 创建 Android 项目骨架与构建配置
   - 使用 Kotlin DSL 编写 `build.gradle.kts`，引入 Jetpack Compose (BOM)、Hilt、Room、OkHttp、Coroutines、Navigation-Compose、DataStore 依赖
   - 创建包结构: `core/data`, `core/domain`, `core/model`, `core/serializer`, `feature/conversation`, `feature/settings`, `feature/history`, `service/accessibility`, `service/overlay`, `di`
   - 创建 `AIHelperApplication` 类并添加 `@HiltAndroidApp` 注解
   - 配置 `AndroidManifest.xml` 声明 AccessibilityService、悬浮窗权限、INTERNET、RECORD_AUDIO 权限

- [ ] 2. 检查点 - 确认项目可编译运行，骨架无误 (需 Android SDK 环境)

- [x] 3. 实现核心数据模型 (R1, R2, R4, R5, R8, R9, R11)
  - [x] 3.1 定义 `UIElement` 数据类（含 id, type, text, contentDescription, hint, bounds, isClickable, isEditable, isPassword, isScrollable 等字段）和 `SerializedUITree` 数据类（含 packageName, activityName, elements 列表, timestamp）—— 满足 R1 UI 树结构
  - [x] 3.2 定义 `UIElementReference` 和 `AnalysisResult` 数据类（含 screenDescription, keyElements, suggestionText, actions）—— 满足 R2 分析响应结构
  - [x] 3.3 定义 `Action` sealed class（Click, LongClick, InputText, Swipe, PressBack, ScrollForward, ScrollBackward, OpenApp），使用 elementIndex 引用 UI 元素 —— 满足 R4 Action 类型
  - [x] 3.4 定义 `Message`、`Conversation`、`UITreeRecord` 数据类，Message 含 role 枚举（USER/ASSISTANT）和 timestamp —— 满足 R3 对话上下文、R9 历史记录
  - [x] 3.5 定义 `LLMConfig` 数据类（baseUrl, apiKey, modelName, maxTokens, temperature）和 `OperationMode` 枚举（SUGGESTION, AUTONOMOUS）—— 满足 R8 配置、R11 模式切换
  - [x] 3.6 为 Action 序列化/反序列化编写单元测试
  - [x] 3.7 为 AnalysisResult JSON 反序列化编写单元测试

- [x] 4. 实现 UI 树序列化工具 (R1)
  - [x] 4.1 实现 `UITreeSerializer`，从 AccessibilityNodeInfo 递归遍历提取为 `UIElement` 列表，过滤无意义的装饰节点 —— 满足 R1 元素提取
  - [x] 4.2 实现序列化为 JSON 字符串，用于发送给 LLM —— 满足 R1 数据就绪
  - [x] 4.3 为 UITreeSerializer 序列化完整性编写单元测试

- [x] 5. 实现数据持久化层 (R9, R8, R11)
  - [x] 5.1 编写 Room Entity 类 —— `ConversationEntity`, `MessageEntity`, `UITreeRecordEntity`，含外键关系和索引 —— 满足 R9
  - [x] 5.2 编写 Room DAO 接口 —— `ConversationDao`（增删查 + 按时间倒序分页）、`MessageDao`（按 conversationId 查询）—— 满足 R9
  - [x] 5.3 编写 `AppDatabase` Room 数据库类，声明所有 Entity 和 DAO，配置 migration 策略 —— 满足 R9
  - [x] 5.4 实现 `SettingsDataStore` 用于 LLMConfig 和 OperationMode 的键值持久化 —— 满足 R8、R11
  - [x] 5.5 为 ConversationDao 查询编写单元测试
  - [x] 5.6 为 SettingsDataStore 读写编写单元测试

- [ ] 6. 检查点 - 确认数据库和 DataStore 操作通过测试

- [x] 7. 实现网络层 —— LLM API 接入 (R2, R8)
  - [x] 7.1 实现 `LLMApiService`，封装 OkHttp 客户端，构造 JSON POST 请求（SerializedUITree JSON + 用户 prompt + 对话上下文），使用 Chat Completions 兼容格式 —— 满足 R2 JSON 界面数据发送
  - [x] 7.2 设计并实现 System Prompt 模板，告知 LLM 其角色为 Android 操作助手，约定输出格式为 AnalysisResult JSON —— 满足 R2 分析格式
  - [x] 7.3 实现 `LLMApiService` 的响应解析，将 LLM 返回 JSON 映射为 `AnalysisResult`，处理 4xx/5xx 错误 —— 满足 R2 错误处理
  - [x] 7.4 实现网络状态监听工具类 `NetworkMonitor`，使用 ConnectivityManager 监听网络变化 —— 满足 R10 离线检测
  - [x] 7.5 为 LLMApiService 请求构造和 System Prompt 编写单元测试（mock OkHttp）
  - [x] 7.6 为 AnalysisResult 响应解析编写单元测试

- [x] 8. 实现 Repository 层 (R2, R8, R9, R10)
  - [x] 8.1 实现 `LLMRepository`，组合 `LLMApiService` + `NetworkMonitor`，封装请求队列和重试逻辑 —— 满足 R2、R10
  - [x] 8.2 实现 `ConversationRepository`，封装 `ConversationDao` + `MessageDao`，对外暴露 Flow<List<ConversationSummary>> —— 满足 R9
  - [x] 8.3 实现 `SettingsRepository`，封装 `SettingsDataStore`，提供 LLMConfig 和 OperationMode 的读写 Flow —— 满足 R8、R11
  - [x] 8.4 为 LLMRepository 队列管理和重试逻辑编写单元测试
  - [x] 8.5 为 ConversationRepository CRUD 编写单元测试

- [x] 9. 实现 Domain 层 —— Use Cases (R1, R2, R3, R4, R5, R7, R8, R9, R11)
  - [x] 9.1 实现 `ReadUITreeUseCase`，通过 AIAccessibilityService 获取当前界面 UI 树，调用 UITreeSerializer 序列化 —— 满足 R1
  - [x] 9.2 实现 `FilterSensitiveNodesUseCase`，遍历 SerializedUITree 将 isPassword=true 节点的 text 替换为 "[已隐藏]" —— 满足 R7 敏感字段过滤
  - [x] 9.3 实现 `AnalyzeScreenUseCase`，组合 ReadUITreeUseCase + FilterSensitiveNodesUseCase + LLMRepository —— 满足 R2
  - [x] 9.4 实现 `ExecuteActionUseCase`，接收 List<Action>，通过 AIAccessibilityService 逐条执行，返回 Flow<ActionResult>，检测连续失败暂停 —— 满足 R4、R5
  - [x] 9.5 实现 `ManageConversationUseCase`，创建/追加消息/结束会话，关联 UITreeRecord —— 满足 R9
  - [x] 9.6 实现 `ProcessVoiceInputUseCase`，封装 SpeechRecognizer 调用，将音频转为文本 —— 满足 R3 语音输入
  - [x] 9.7 实现 `ValidateLLMConfigUseCase`，使用配置调用 LLM API 测试端点并返回验证结果 —— 满足 R8 连接验证
  - [x] 9.8 为 AnalyzeScreenUseCase 请求组装逻辑编写单元测试
  - [x] 9.9 为 ExecuteActionUseCase 失败计数和暂停逻辑编写单元测试
  - [x] 9.10 为 FilterSensitiveNodesUseCase 过滤逻辑编写单元测试

- [ ] 10. 检查点 - 确认所有 Use Case 覆盖需求

- [x] 11. 实现 AccessibilityService (R1, R4, R5, R7)
  - [x] 11.1 创建 `AIAccessibilityService` 继承 AccessibilityService，注册 `accessibility_service_config.xml`，监听 TYPE_WINDOW_STATE_CHANGED 和 TYPE_WINDOW_CONTENT_CHANGED 事件 —— 满足 R1、R4
  - [x] 11.2 实现 UI 树读取功能 —— `readUITree()` 方法，通过 rootInActiveWindow 获取根节点，递归遍历提取元素属性 —— 满足 R1
  - [x] 11.3 实现 `performAction(Action)` 方法，根据 Action 类型（Click/InputText/Swipe/Scroll/Back）调用 AccessibilityNodeInfo 对应方法，使用 elementIndex 定位元素 —— 满足 R4 操作类型
  - [x] 11.4 实现操作执行结果回调机制，通过 SharedFlow 报告每步操作成功/失败 —— 满足 R4 进度反馈
  - [x] 11.5 实现敏感操作检测逻辑，识别支付/删除对话框并触发二次确认回调 —— 满足 R7 敏感操作确认

- [x] 12. 实现悬浮球服务 (R6)
  - [x] 12.1 实现 `OverlayService`，通过 WindowManager 在系统层级添加可拖拽悬浮球 View —— 满足 R6
  - [x] 12.2 实现悬浮球交互事件 —— 点击触发 UI 树读取并打开对话界面，长按触发语音输入 —— 满足 R6
  - [x] 12.3 实现悬浮球折叠与通知栏入口，折叠时最小化至通知栏并支持一键恢复 —— 满足 R6

- [ ] 13. 检查点 - 确认服务层独立可运行，悬浮球点击可触发 UI 树读取

- [x] 14. 实现 UI 层 —— 导航与主题
   - 定义 Compose 主题（Material 3，颜色/字体/形状），配置 Navigation 路由（home, conversation, settings, history）—— 满足全部 UI 需求
   - 实现 `MainActivity` 作为单 Activity 入口，组装 NavHost 和权限请求流程（仅无障碍 + 悬浮窗）—— 满足 R7 首次启动权限引导

- [x] 15. 实现对话界面 (R2, R3, R4, R5, R11)
  - [x] 15.1 实现 `ConversationViewModel`，管理对话状态（消息列表、当前 UI 树、操作模式、LLM 请求状态），对接 AnalyzeScreenUseCase、ExecuteActionUseCase、ManageConversationUseCase、ProcessVoiceInputUseCase —— 满足 R2、R3、R4、R5、R11
  - [x] 15.2 实现 `ConversationScreen` Compose 界面 —— 顶部显示当前应用名和界面摘要、消息气泡列表（用户/AI 区分样式）、输入栏、发送按钮 —— 满足 R3
  - [x] 15.3 实现 `ModeSwitchChip` 组件，在对话界面顶部显示当前操作模式并支持点击切换（建议/自主），切换后立即生效并持久化 —— 满足 R11
  - [x] 15.4 实现操作建议卡片组件（仅建议模式下显示），展示步骤说明 + 「采纳执行」「忽略」按钮 —— 满足 R5
  - [x] 15.5 实现操作进度指示器，在自主执行模式下按步骤更新操作状态 —— 满足 R4
  - [x] 15.6 实现语音输入功能，长按录音按钮触发 SpeechRecognizer，显示波形动画和实时文字 —— 满足 R3
  - [x] 15.7 实现 LLM 请求中状态动画（思考指示器）和取消请求按钮 —— 满足 R2

- [x] 16. 实现设置界面 (R8)
  - [x] 16.1 实现 `SettingsViewModel`，管理 LLMConfig 表单状态和 OperationMode 偏好 —— 满足 R8、R11
  - [x] 16.2 实现 `SettingsScreen` Compose 界面 —— API 地址输入、API Key 输入（密码字段）、模型名称输入、连接验证按钮、操作模式切换 —— 满足 R8
  - [x] 16.3 实现连接验证交互，点击「验证连接」后调用 ValidateLLMConfigUseCase 并展示验证结果 —— 满足 R8

- [x] 17. 实现历史记录界面 (R9)
  - [x] 17.1 实现 `HistoryViewModel`，加载 ConversationRepository 的对话历史流 —— 满足 R9
  - [x] 17.2 实现 `HistoryScreen` Compose 界面 —— 时间倒序列表（对话摘要 + 时间 + 界面简要描述），点击进入详情，滑动删除 —— 满足 R9
  - [x] 17.3 实现历史详情界面，加载完整对话消息和对应界面描述 —— 满足 R9

- [x] 18. 实现首页 (R7, R10)
   - 实现 `HomeScreen` Compose 界面 —— 应用简介、功能入口卡片（开始对话 / 历史记录 / 设置）、无障碍服务状态显示 —— 满足 R7 权限引导
   - 实现首页中的离线提示横幅组件，监听 NetworkMonitor 状态 —— 满足 R10

- [x] 19. 实现 DI 模块
   - 编写 Hilt Module 类 (`AppModule`)，提供 Room 数据库、OkHttp、AccessibilityServiceBridge 的单例依赖 —— 满足架构 DI 需求

- [x] 20. 实现错误处理与边界逻辑 (R2, R4, R7, R10)
    - 实现全局异常处理，LLM API 超时自动取消 + 重试按钮，网络不可用入队列 + 恢复自动重试 —— 满足 R2、R10
    - 实现无障碍服务状态检测，未开启时禁止自动操作并跳转系统设置 —— 满足 R7
    - 实现 FIFO 缓存清理策略，对话记录超过 20 条时清理最早记录 —— 满足 R7、R9
    - 实现 Action elementIndex 越界检测，越界时跳过该操作并继续执行后续 —— 满足 R4

- [ ] 21. 检查点 - 全功能联调，确认所有需求路径可走通

- [x] 22. 端到端集成与收尾
   - 编写 `proguard-rules.pro` 混淆规则，确保 OkHttp、Room、Hilt 不被混淆
   - 验证 AndroidManifest 所有组件声明、权限声明、intent-filter 正确
   - 配置多语言资源文件 (`strings.xml`) 提取界面硬编码字符串

- [x] 23. UI 与集成测试
  - [x] 23.1 为 ConversationScreen 编写 Compose UI 测试 —— 消息发送、模式切换、操作建议卡片渲染
  - [x] 23.2 为 SettingsScreen 编写 Compose UI 测试 —— 表单输入、连接验证按钮
  - [x] 23.3 为 HistoryScreen 编写 Compose UI 测试 —— 列表加载、删除操作
  - [x] 23.4 编写 LLMRepository + Mock LLM Server 集成测试，验证请求 JSON 格式和错误码处理
  - [x] 23.5 编写 ConversationScreen 完整流程集成测试（mock LLM 响应）
