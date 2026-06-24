# UI 导航模块

为单 Activity 应用提供 Compose Navigation 路由和图定义。

## 文件

| 文件 | 职责 |
|------|------|
| `Screen.kt` | 路由常量 sealed class，定义 4 个路由：`Home`, `Conversation`, `Settings`, `History` |
| `NavGraph.kt` | `AppNavGraph` composable，装配 NavHost，将路由映射到 Screen composable，通过 `hiltViewModel()` 获取各页面 ViewModel |

## 路由表

| 路由 | 目标 Screen | ViewModel |
|------|-----------|-----------|
| `home` | `HomeScreen` | 无（纯展示） |
| `conversation` | `ConversationScreen` | `ConversationViewModel` |
| `settings` | `SettingsScreen` | `SettingsViewModel` |
| `history` | `HistoryScreen` | `HistoryViewModel` |

## AppNavGraph 参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `navController` | `NavHostController` | 导航控制器 |
| `accessibilityEnabled` | `Boolean` | 无障碍服务是否开启 |
| `llmConfigured` | `Boolean` | LLM 配置是否完成 |
| `isOnline` | `Boolean` | 网络是否可用 |

所有 Screen 通过 `collectAsState()` 观察 ViewModel 的 StateFlow 实现响应式更新。
