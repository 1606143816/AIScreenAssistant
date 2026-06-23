# Requirements Document

## Introduction

AI 屏幕助手是一款 Android 应用，通过截取手机当前屏幕内容并发送至云端大语言模型进行分析理解，为用户提供操作建议或代替用户执行屏幕操作（如点击、输入、滑动等）。应用的核心价值在于让用户通过自然语言与手机交互，降低操作门槛，帮助视障用户、老年人或任何需要辅助的场景。

## Glossary

- **System / App / 应用**: AI 屏幕助手 Android 应用
- **LLM**: 云端大语言模型（Large Language Model）
- **Screen Capture / 截屏**: 获取手机当前屏幕显示内容的图像
- **Accessibility Service / 无障碍服务**: Android 系统提供的无障碍服务，用于读取界面元素、执行模拟操作
- **Floating Button / 悬浮球**: 始终显示在其他应用上方的可拖拽操作入口
- **Action / 操作**: 应用在用户手机上执行的自动化操作（点击、输入、滑动、返回等）
- **Conversation / 对话**: 用户与 LLM 之间一次连续的交互记录

## Requirements

### R1 - 屏幕内容获取

**User Story:** AS 用户, I want 应用获取我当前屏幕的图像内容, so that LLM 可以理解当前界面并给出有效建议。

#### Acceptance Criteria

1. WHEN 用户通过悬浮球或通知栏触发截屏，THE 应用 SHALL 捕获当前屏幕的完整图像。
2. WHEN 截屏请求发起，THE 应用 SHALL 在 500ms 内完成屏幕图像数据的就绪。
3. IF 用户拒绝截屏所需权限，THE 应用 SHALL 显示明确的权限引导提示并退出当前流程。
4. WHILE 截屏正在进行，THE 应用 SHALL 显示加载状态指示器。

---

### R2 - 屏幕内容分析

**User Story:** AS 用户, I want 应用将屏幕截图发送给云端 LLM 并获取关于当前界面的分析结果, so that 我能获得针对当前屏幕的智能反馈。

#### Acceptance Criteria

1. WHEN 屏幕截图就绪，THE 应用 SHALL 将图像数据与用户的自然语言指令一并发送至云端 LLM 接口。
2. WHEN LLM 返回分析结果，THE 应用 SHALL 在对话界面中展示分析内容（包括界面描述、可操作元素识别、操作建议）。
3. IF 网络不可用，THE 应用 SHALL 向用户提示网络连接异常并保存当前请求供后续重试。
4. IF LLM 接口返回错误响应，THE 应用 SHALL 向用户展示错误信息并提供重试选项。
5. WHILE 等待 LLM 响应，THE 应用 SHALL 显示思考状态动画并允许用户取消请求。

---

### R3 - 自然语言对话交互

**User Story:** AS 用户, I want 通过文字或语音与应用进行自然语言对话, so that 我可以自由表达操作意图并获得帮助。

#### Acceptance Criteria

1. WHEN 用户输入文字消息，THE 应用 SHALL 将文本与当前屏幕上下文一并提交至 LLM。
2. WHEN 用户长按悬浮球，THE 应用 SHALL 启动内置语音识别引擎并实时采集用户语音。
3. WHEN 语音采集完成，THE 应用 SHALL 使用 Android SpeechRecognizer 将语音转写为文本后提交至 LLM。
4. WHILE 语音识别进行中，THE 应用 SHALL 显示语音波形动画和实时识别文字预览。
5. WHILE 对话进行中，THE 应用 SHALL 维护完整的对话上下文历史直至用户主动结束会话。

---

### R4 - 自动化操作执行

**User Story:** AS 用户, I want 应用根据 LLM 的分析结果代替我执行手机操作, so that 我无需手动操作即可完成目标。

#### Acceptance Criteria

1. WHEN LLM 返回包含操作指令的响应，THE 应用 SHALL 通过 Android Accessibility Service 执行对应的操作（包括点击指定坐标、输入指定文本、滑动屏幕、按下返回键、滚动列表）。
2. WHEN 操作涉及敏感权限操作（如支付、删除），THE 应用 SHALL 先向用户请求二次确认后再执行。
3. IF 目标 UI 元素在当前屏幕不可见，THE 应用 SHALL 向用户提示操作无法完成并请求重新截屏。
4. WHILE 执行自动操作，THE 应用 SHALL 实时更新操作进度信息。
5. IF 连续 3 次操作执行失败，THE 应用 SHALL 暂停自动操作并提示用户手动介入。

---

### R5 - 操作建议模式

**User Story:** AS 用户, I want 应用提供操作建议而不直接执行, so that 我可以自主决定是否采纳 LLM 的建议。

#### Acceptance Criteria

1. WHEN LLM 返回操作建议，THE 应用 SHALL 以卡片形式展示每个建议的步骤说明和目标。
2. WHILE 显示操作建议，THE 应用 SHALL 为每条建议提供 "采纳执行" 和 "忽略" 两个操作按钮。
3. WHEN 用户点击 "采纳执行"，THE 应用 SHALL 仅执行该条建议对应的操作。

---

### R6 - 悬浮球交互入口

**User Story:** AS 用户, I want 一个悬浮在所有应用之上的操作入口, so that 我可以在任何场景下快速唤起 AI 助手。

#### Acceptance Criteria

1. WHILE 应用无障碍服务已启用，THE 应用 SHALL 在屏幕边缘显示一个半透明悬浮球。
2. WHEN 用户点击悬浮球，THE 应用 SHALL 截取当前屏幕并打开对话界面。
3. WHEN 用户拖拽悬浮球，THE 应用 SHALL 允许用户改变悬浮球在屏幕边缘的位置。
4. WHEN 用户长按悬浮球，THE 应用 SHALL 启动语音输入模式。
5. IF 用户折叠/关闭悬浮球，THE 应用 SHALL 最小化至通知栏并支持一键恢复。

---

### R7 - 权限与安全

**User Story:** AS 用户, I want 应用在安全可控的前提下操作我的手机, so that 我的隐私和数据不被滥用。

#### Acceptance Criteria

1. WHEN 应用首次启动，THE 应用 SHALL 依次引导用户授予无障碍服务权限、悬浮窗权限、截屏权限。
2. WHEN 屏幕截图被发送至 LLM 前，THE 应用 SHALL 自动模糊处理检测到的密码输入框区域的图像内容。
3. IF 用户未授予无障碍权限，THE 应用 SHALL 限制自动操作功能但仍提供操作建议模式。
4. WHILE 应用处理屏幕数据，THE 应用 SHALL 在本地缓存中存储最近 20 条对话记录且不向其他应用暴露。

---

### R8 - LLM 服务配置

**User Story:** AS 用户, I want 配置连接的大模型服务, so that 我可以使用自己的 API Key 或切换不同的模型。

#### Acceptance Criteria

1. WHEN 用户进入设置页面，THE 应用 SHALL 提供 LLM 服务配置入口（API 地址、API Key、模型名称）。
2. WHEN 用户保存配置，THE 应用 SHALL 验证连接可用性并反馈验证结果。
3. WHILE 配置未完成或无效，THE 应用 SHALL 在对话界面显示配置引导提示。

---

### R9 - 对话历史

**User Story:** AS 用户, I want 查看过往的对话历史, so that 我可以回顾之前的操作记录和分析结果。

#### Acceptance Criteria

1. WHEN 用户进入历史页面，THE 应用 SHALL 以时间倒序展示对话列表（包含对话摘要、时间、截图缩略图）。
2. WHEN 用户点击某条历史记录，THE 应用 SHALL 加载完整对话内容和对应截图。
3. WHEN 用户执行删除操作，THE 应用 SHALL 清除对应对话记录及相关截屏缓存。

---

### R10 - 离线与网络适配

**User Story:** AS 用户, I want 在网络不稳定时获得合理的体验, so that 应用不会因网络波动完全不可用。

#### Acceptance Criteria

1. IF 网络断开，THE 应用 SHALL 在界面顶部显示离线提示横幅。
2. WHEN 网络恢复，THE 应用 SHALL 自动重连并重试失败队列中的请求。
3. WHILE 离线状态，THE 应用 SHALL 允许用户浏览历史对话记录。

---

### R11 - 操作模式切换

**User Story:** AS 用户, I want 在建议模式和自主执行模式之间自由切换, so that 我可以根据场景选择最合适的 AI 交互方式。

#### Acceptance Criteria

1. WHEN 用户在对话界面切换操作模式，THE 应用 SHALL 提供 "建议模式" 和 "自主执行模式" 两个选项。
2. WHILE 处于建议模式，THE 应用 SHALL 仅提供操作建议卡片并等待用户确认（遵循 R5）。
3. WHILE 处于自主执行模式，THE 应用 SHALL 自动执行 LLM 返回的操作指令（遵循 R4）。
4. WHEN 用户切换操作模式，THE 应用 SHALL 在对话界面以醒目标识提示当前模式状态。
5. WHILE 应用运行中，THE 应用 SHALL 持久化保存用户选择的操作模式偏好。
